package com.jumbo.routeplanner.gateway.google;

import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.domain.exception.GoogleApiException;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import com.jumbo.routeplanner.gateway.google.converter.GoogleResponseWrapperToRoute;
import com.jumbo.routeplanner.gateway.google.to.GoogleResponseWrapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.jumbo.routeplanner.configuration.metrics.MetricsHelper.recordDuration;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.StringUtils.hasText;

@Component
@Slf4j
@RequiredArgsConstructor
public class GooglePostalCodeGatewayImpl implements RouteplannerGateway {

    private static final int MAX_GOOGLE_BULK_CONSULT = 20;
    private static final String GOOGLEAPI = "googleapi";
    private static final String GEOCODING = "geocoding";
    private static final String DISTANCEMATRIX = "distancematrix";
    private static final String API = "api";
    private final GeoApiContext geoApiContext;
    private final GoogleResponseWrapperToRoute googleResponseWrapperToTimeAndDistance;
    private final MeterRegistry meterRegistry;

    @Override
    public Optional<LatLong> getMapLocationByPostalCode(String postalCode, String countryCode, String street) {
        try {

            log.info("Reaching Google to get info for postalcode {} countryCode {} and street {} ", postalCode, countryCode, street);
            final GeocodingResult[] geocodingResults = getGoogleGeocodingResults(postalCode, countryCode, street);

            return Arrays.stream(geocodingResults)
                    .map(geocodingResult -> LatLong.builder()
                            .lat(geocodingResult.geometry.location.lat)
                            .lng(geocodingResult.geometry.location.lng)
                            .build())
                    .findFirst();

        } catch (Exception e) {
            log.error("Failed to get latitude and longitude for {}", postalCode, e);
            throw new GoogleApiException(e);
        }
    }

    @Override
    public Collection<Route> getRoutesForMultipleOriginsToSingleDestination(final List<LatLong> origins, final LatLong destination) {
        final String googleValidationErrorMessage = String.format("Google support at most %s queries at a time", MAX_GOOGLE_BULK_CONSULT);
        isTrue(origins.size() <= MAX_GOOGLE_BULK_CONSULT, googleValidationErrorMessage);

        final LatLng[] googleOrigins = origins.stream()
                .map(origin -> new LatLng(origin.getLat(), origin.getLng()))
                .toArray(LatLng[]::new);

        final LatLng googleDestination =
                new LatLng(destination.getLat(), destination.getLng());

        log.info("Reaching Google to calculate Time and Distance for origin {} destination(s) {}", origins, destination);

        final DistanceMatrixApiRequest distanceMatrix =
                DistanceMatrixApi.newRequest(geoApiContext)
                        .origins(googleOrigins)
                        .destinations(googleDestination);

        try {
            final DistanceMatrix calculatedTimeAndDistanceMatrix = recordDuration(meterRegistry,
                    () -> {
                        try {
                            return distanceMatrix.await();
                        } catch (ApiException e) {
                            throw new GoogleApiException(e);
                        } catch (InterruptedException e) {
                            throw new GoogleApiException(e);
                        } catch (IOException e) {
                            throw new GoogleApiException(e);
                        }
                    },
                    GOOGLEAPI,
                    API, DISTANCEMATRIX);

            final GoogleResponseWrapper googleResponseWrapper = GoogleResponseWrapper.builder()
                    .originLatAndLongList(origins)
                    .destinationLatAndLong(destination)
                    .distanceMatrixResponse(calculatedTimeAndDistanceMatrix)
                    .build();

            return googleResponseWrapperToTimeAndDistance.convert(googleResponseWrapper);

        } catch (Exception e) {
            log.error("Failed to calculated Time And Distance for origin {} destination {}", origins, destination, e);
            throw new GoogleApiException(e);
        }
    }

    private GeocodingResult[] getGoogleGeocodingResults(String postalCode, String countryCode, String street) {

        final GeocodingApiRequest geocodingApiRequest =
                GeocodingApi.newRequest(geoApiContext).components(
                        ComponentFilter.country(countryCode),
                        ComponentFilter.postalCode(postalCode));

        if (hasText(street)) {
            geocodingApiRequest.address(street);
        }

        return recordDuration(meterRegistry,
                () -> {
                    try {
                        return geocodingApiRequest.await();
                    } catch (ApiException e) {
                        throw new GoogleApiException(e);
                    } catch (InterruptedException e) {
                        throw new GoogleApiException(e);
                    } catch (IOException e) {
                        throw new GoogleApiException(e);
                    }
                },
                GOOGLEAPI,
                API, GEOCODING);
    }

}
