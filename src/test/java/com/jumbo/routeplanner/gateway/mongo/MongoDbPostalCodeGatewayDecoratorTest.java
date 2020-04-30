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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class MongoDbPostalCodeGatewayDecoratorTest {

    private RouteplannerGateway decorated;
    private PostalCodeRepository postalCodeRepository;
    private MongoDbPostalCodeGatewayDecorator mongoDbPostalCodeGatewayDecorator;
    private LatLongDistanceAndTimeDocumentToRoute latLongDistanceAndTimeDocumentToRoute;
    private RouteToLatLongDistanceAndTimeDocument routeToLatLongDistanceAndTimeDocument;
    private LatLongDistanceAndTimeRepository latLongDistanceAndTimeRepository;
    private MeterRegistry meterRegistry;

    @Captor
    private ArgumentCaptor<ArrayList<LatLongDistanceAndTimeDocument>> captor;

    @Before
    public void setUp() {
        decorated = mock(RouteplannerGateway.class);
        postalCodeRepository = mock(PostalCodeRepository.class);
        meterRegistry = new SimpleMeterRegistry();
        latLongDistanceAndTimeRepository = mock(LatLongDistanceAndTimeRepository.class);
        routeToLatLongDistanceAndTimeDocument = new RouteToLatLongDistanceAndTimeDocument();
        latLongDistanceAndTimeDocumentToRoute = new LatLongDistanceAndTimeDocumentToRoute();
        mongoDbPostalCodeGatewayDecorator = new MongoDbPostalCodeGatewayDecorator(
                decorated,
                postalCodeRepository,
                latLongDistanceAndTimeRepository,
                latLongDistanceAndTimeDocumentToRoute,
                routeToLatLongDistanceAndTimeDocument,
                meterRegistry);
    }

    @Test
    public void shouldReturnResultFromMongoDbWhenRepositoryContainsMatchingStoredDocument() {
        // GIVEN a repository which contains a matching stored document
        PostalCodeDocument postalCodeDocument = PostalCodeDocument.builder()
                .postalCodeID(PostalCodeID.builder()
                        .postalCode("1000")
                        .countryCode("NL")
                        .build())
                .longitude(1.0)
                .latitude(1.0)
                .build();

        when(postalCodeRepository.findById(any())).thenReturn(Optional.of(postalCodeDocument));

        // WHEN
        Optional<LatLong> actual = mongoDbPostalCodeGatewayDecorator.getMapLocationByPostalCode(
                "1000",
                "NL",
                null);

        // THEN
        Optional<LatLong> expected = Optional.of(new LatLong(1.0, 1.0));
        verify(decorated, never()).getMapLocationByPostalCode(any(), any(), any());
        then(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptyWhenDocumentContainsNullLatitudeLongitude() {
        // GIVEN a repository which contains a matching stored document
        PostalCodeDocument postalCodeDocument = PostalCodeDocument.builder()
                .postalCodeID(PostalCodeID.builder()
                        .postalCode("1000")
                        .countryCode("NL")
                        .build())
                .build();

        when(postalCodeRepository.findById(any())).thenReturn(Optional.of(postalCodeDocument));

        // WHEN
        Optional<LatLong> actual = mongoDbPostalCodeGatewayDecorator.getMapLocationByPostalCode(
                "1000",
                "NL",
                null);

        // THEN
        verify(decorated, never()).getMapLocationByPostalCode(any(), any(), any());
        then(actual).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldReturnResultFromDecoratedWhenRepositoryNotContainsMatchingStoredDocument() {
        // GIVEN a repository which does not contain a matching stored document
        when(postalCodeRepository.findById(any())).thenReturn(Optional.empty());

        // WHEN
        mongoDbPostalCodeGatewayDecorator.getMapLocationByPostalCode(
                "1000",
                "NL",
                null);

        // THEN
        verify(decorated, times(1)).getMapLocationByPostalCode(any(), any(), any());
    }

    @Test
    public void shouldSaveWhenDecoratedDoesNotReturnResult() {
        // GIVEN a repository which does not contain a matching stored document
        when(latLongDistanceAndTimeRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // WHEN try to retrieve a route for non existing route locally and externally
        Collection<Route> actual = mongoDbPostalCodeGatewayDecorator.getRoutesForMultipleOriginsToSingleDestination(
                Collections.singletonList(new LatLong(1.1, 1.2)),
                new LatLong(1.3, 1.4));

        // THEN it should be cached anyway with no distance and time
        verify(decorated, times(1)).getRoutesForMultipleOriginsToSingleDestination(
                Collections.singletonList(new LatLong(1.1, 1.2)),
                new LatLong(1.3, 1.4));

        verify(latLongDistanceAndTimeRepository, times(1)).saveAll(captor.capture());

        ArrayList<LatLongDistanceAndTimeDocument> savedDocuments = captor.getValue();
        then(savedDocuments.size()).isEqualTo(1);

        LatLongDistanceAndTimeDocument savedDocument = savedDocuments.stream().findFirst().get();
        then(savedDocument.getDistanceTimeID().getOriginLatitude()).isEqualTo(1.1);
        then(savedDocument.getDistanceTimeID().getOriginLongitude()).isEqualTo(1.2);
        then(savedDocument.getDistanceTimeID().getDestinationLatitude()).isEqualTo(1.3);
        then(savedDocument.getDistanceTimeID().getDestinationLongitude()).isEqualTo(1.4);
        then(savedDocument.getCreatedDate()).isNotNull();

        then(actual.size()).isZero();
    }

    @Test
    public void shouldNotSaveOrCallDecoratedWhenCachedReturnsResult() {
        // GIVEN a repository which does contain a matching stored document
        when(latLongDistanceAndTimeRepository.findAllById(any())).thenReturn(
                Collections.singletonList(
                        LatLongDistanceAndTimeDocument.builder()
                                .distanceTimeID(LatLongDistanceAndTimeID.builder()
                                        .originLatitude(1.0)
                                        .originLongitude(1.0)
                                        .destinationLatitude(1.0)
                                        .destinationLongitude(1.0)
                                        .build())
                                .pathDistanceInMeters(1.0)
                                .distanceTimeInSeconds(1L)
                                .build()
                ));

        // WHEN
        Collection<Route> actual = mongoDbPostalCodeGatewayDecorator.getRoutesForMultipleOriginsToSingleDestination(
                Collections.singletonList(new LatLong(1.0, 1.0)),
                new LatLong(1.0, 1.0));

        // THEN
        verify(decorated, never()).getRoutesForMultipleOriginsToSingleDestination(any(), any());
        verify(latLongDistanceAndTimeRepository, never()).saveAll(any());
        then(actual.size()).isOne();
    }

    @Test
    public void shouldReturnNoResultWhenDocumentIsEmpty() {
        // GIVEN a repository which does contain a matching stored document
        when(latLongDistanceAndTimeRepository.findAllById(any())).thenReturn(
                Collections.singletonList(
                        LatLongDistanceAndTimeDocument.builder()
                                .distanceTimeID(LatLongDistanceAndTimeID.builder()
                                        .originLatitude(1.0)
                                        .originLongitude(1.0)
                                        .destinationLatitude(1.0)
                                        .destinationLongitude(1.0)
                                        .build())
                                .build()
                ));

        // WHEN
        Collection<Route> actual = mongoDbPostalCodeGatewayDecorator.getRoutesForMultipleOriginsToSingleDestination(
                Collections.singletonList(new LatLong(1.0, 1.0)),
                new LatLong(1.0, 1.0));

        // THEN
        verify(decorated, never()).getRoutesForMultipleOriginsToSingleDestination(any(), any());
        verify(latLongDistanceAndTimeRepository, never()).saveAll(any());
        then(actual.size()).isEqualTo(0);
    }

    @Test
    public void shouldSaveOneWhenOneNotInRepository() {
        // GIVEN a repository which does contain one matching stored document
        when(latLongDistanceAndTimeRepository.findAllById(any())).thenReturn(
                Collections.singletonList(
                        LatLongDistanceAndTimeDocument.builder()
                                .distanceTimeID(LatLongDistanceAndTimeID.builder()
                                        .originLatitude(1.0)
                                        .originLongitude(2.0)
                                        .destinationLatitude(3.0)
                                        .destinationLongitude(4.0)
                                        .build())
                                .build()
                ));

        // WHEN requesting for multiple origins
        mongoDbPostalCodeGatewayDecorator.getRoutesForMultipleOriginsToSingleDestination(
                Arrays.asList(new LatLong(1.0, 2.0), new LatLong(8.0, 9.0)),
                new LatLong(3.0, 4.0));

        // THEN expect one origin to be saved
        verify(decorated, times(1)).getRoutesForMultipleOriginsToSingleDestination(any(), any());

        verify(latLongDistanceAndTimeRepository, times(1)).saveAll(captor.capture());

        ArrayList<LatLongDistanceAndTimeDocument> savedDocuments = captor.getValue();
        then(savedDocuments.size()).isEqualTo(1);

        LatLongDistanceAndTimeDocument savedDocument = savedDocuments.stream().findFirst().get();
        then(savedDocument.getDistanceTimeID().getOriginLatitude()).isEqualTo(8.0);
        then(savedDocument.getDistanceTimeID().getOriginLongitude()).isEqualTo(9.0);
        then(savedDocument.getDistanceTimeID().getDestinationLatitude()).isEqualTo(3.0);
        then(savedDocument.getDistanceTimeID().getDestinationLongitude()).isEqualTo(4.0);
        then(savedDocument.getCreatedDate()).isNotNull();
    }
}