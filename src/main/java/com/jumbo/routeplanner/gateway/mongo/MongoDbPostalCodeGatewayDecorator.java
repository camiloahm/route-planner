package com.jumbo.routeplanner.gateway.mongo;

import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import com.jumbo.routeplanner.gateway.mongo.converter.LatLongDistanceAndTimeDocumentToRoute;
import com.jumbo.routeplanner.gateway.mongo.converter.RouteToLatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeID;
import com.jumbo.routeplanner.gateway.mongo.repository.LatLongDistanceAndTimeRepository;
import com.jumbo.routeplanner.gateway.mongo.repository.PostalCodeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.jumbo.routeplanner.configuration.metrics.MetricsHelper.recordDuration;

@RequiredArgsConstructor
@Slf4j
public class MongoDbPostalCodeGatewayDecorator implements RouteplannerGateway {

    private static final String MONGODB = "mongodb";
    private static final String OPERATION = "operation";
    private static final String COLLECTION = "collection";
    private final RouteplannerGateway decorated;
    private final PostalCodeRepository postalCodeRepository;
    private final LatLongDistanceAndTimeRepository latLongDistanceAndTimeRepository;
    private final LatLongDistanceAndTimeDocumentToRoute latLongDistanceAndTimeDocumentToRoute;
    private final RouteToLatLongDistanceAndTimeDocument routeToLatLongDistanceAndTimeDocument;
    private final MeterRegistry meterRegistry;

    @Override
    public Optional<LatLong> getMapLocationByPostalCode(final String postalCode, final String countryCode, final String street) {

        final Optional<PostalCodeDocument> document = findPostCodeById(PostalCodeID.builder()
                .countryCode(countryCode)
                .streetName(street)
                .postalCode(postalCode)
                .build());

        if (document.isPresent()) {
            if (document.get().getLatitude() == null && document.get().getLongitude() == null) {
                return Optional.empty();
            }

            return document.map(p -> LatLong.builder()
                    .lat(p.getLatitude())
                    .lng(p.getLongitude())
                    .build());
        }

        final Optional<LatLong> latLongOption = decorated.getMapLocationByPostalCode(postalCode, countryCode, street);
        final LatLong latlong = latLongOption.orElse(new LatLong());
        savePostalCode(postalCode, countryCode, street, latlong);

        return latLongOption;
    }

    private LatLongDistanceAndTimeID createLatLongDistanceAndTimeID(LatLong origin, LatLong destination) {
        return LatLongDistanceAndTimeID.builder()
                .originLatitude(origin.getLat())
                .originLongitude(origin.getLng())
                .destinationLatitude(destination.getLat())
                .destinationLongitude(destination.getLng())
                .build();
    }

    @Override
    public Collection<Route> getRoutesForMultipleOriginsToSingleDestination(final List<LatLong> origins, final LatLong destination) {
        List<LatLongDistanceAndTimeID> ids = origins.stream()
                .map(origin -> createLatLongDistanceAndTimeID(origin, destination))
                .collect(Collectors.toList());

        final Iterable<LatLongDistanceAndTimeDocument> documents = findLatLongDistanceAndTimeByIds(ids);

        final List<Route> routes = StreamSupport.stream(documents.spliterator(), false)
                .map(latLongDistanceAndTimeDocumentToRoute::convert)
                .collect(Collectors.toList());

        final List<LatLong> originsInRoutes = routes.stream()
                .map(Route::getOrigin)
                .collect(Collectors.toList());

        final List<LatLong> notFoundInRepository = origins.stream()
                .filter(origin -> !originsInRoutes.contains(origin))
                .collect(Collectors.toList());

        if (!notFoundInRepository.isEmpty()) {

            final Collection<Route> routesFromDecorated = decorated.getRoutesForMultipleOriginsToSingleDestination(notFoundInRepository, destination);
            routes.addAll(routesFromDecorated);

            final List<LatLong> stillMissingOrigins = routes.stream()
                    .map(Route::getOrigin)
                    .collect(Collectors.toList());

            final List<Route> notFoundInDecorated = origins.stream()
                    .filter(origin -> !stillMissingOrigins.contains(origin))
                    .map(origin -> Route.builder()
                            .origin(origin)
                            .destination(destination)
                            .build())
                    .collect(Collectors.toList());

            final Collection<Route> routesToSave = Stream.of(routesFromDecorated, notFoundInDecorated)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            saveRoutes(routesToSave);
        }

        return routes.stream()
                .filter(route -> route.getDistanceInMeters() != null)
                .filter(route -> route.getTimeInSeconds() != null)
                .collect(Collectors.toList());
    }

    private Optional<PostalCodeDocument> findPostCodeById(PostalCodeID id) {
        log.debug("findById {}", id);
        return recordDuration(meterRegistry,
                () -> postalCodeRepository.findById(id),
                MONGODB,
                OPERATION,
                "findById",
                COLLECTION,
                "PostalCode");
    }

    private void savePostalCode(final String postalCode, final String countryCode, final String street, final LatLong latlong) {
        final PostalCodeDocument document = PostalCodeDocument.builder()
                .latitude(latlong.getLat())
                .longitude(latlong.getLng())
                .createdDate(LocalDateTime.now())
                .postalCodeID(
                        PostalCodeID.builder()
                                .countryCode(countryCode)
                                .postalCode(postalCode)
                                .streetName(street)
                                .build())
                .build();
        log.debug("save {}", document);
        recordDuration(meterRegistry,
                () -> postalCodeRepository.save(document),
                MONGODB,
                OPERATION, "save",
                COLLECTION, "PostalCode");
    }

    private Iterable<LatLongDistanceAndTimeDocument> findLatLongDistanceAndTimeByIds(final List<LatLongDistanceAndTimeID> ids) {
        log.debug("findAllById {}", ids);
        return recordDuration(meterRegistry,
                () -> latLongDistanceAndTimeRepository.findAllById(ids),
                MONGODB,
                OPERATION, "findAllById",
                COLLECTION, "LatLongDistanceAndTime");
    }

    private void saveRoutes(final Collection<Route> routes) {
        if (!routes.isEmpty()) {
            final List<LatLongDistanceAndTimeDocument> documents = routes.stream()
                    .map(routeToLatLongDistanceAndTimeDocument::convert)
                    .collect(Collectors.toList());

            documents.forEach(document -> document.setCreatedDate(LocalDateTime.now()));

            log.debug("saveAll {}", documents);

            recordDuration(meterRegistry,
                    () -> latLongDistanceAndTimeRepository.saveAll(documents),
                    MONGODB,
                    OPERATION, "saveAll",
                    COLLECTION, "LatLongDistanceAndTime");
        }
    }
}
