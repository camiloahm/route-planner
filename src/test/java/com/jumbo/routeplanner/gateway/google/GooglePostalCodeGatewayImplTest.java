package com.jumbo.routeplanner.gateway.google;

import com.google.maps.GeoApiContext;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.exception.GoogleApiException;
import com.jumbo.routeplanner.gateway.google.converter.GoogleResponseWrapperToRoute;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;

public class GooglePostalCodeGatewayImplTest {

    private GeoApiContext geoApiContext;
    private GoogleResponseWrapperToRoute googleResponseWrapperToTimeAndDistance;
    private MeterRegistry meterRegistry;

    private GooglePostalCodeGatewayImpl googlePostalCodeGateway;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setupTest() {
        geoApiContext = mock(GeoApiContext.class);
        googleResponseWrapperToTimeAndDistance = new GoogleResponseWrapperToRoute();
        meterRegistry = new SimpleMeterRegistry();
        googlePostalCodeGateway = new GooglePostalCodeGatewayImpl(geoApiContext, googleResponseWrapperToTimeAndDistance, meterRegistry);
    }

    @Test
    public void shouldReturnErrorDueToMoreThanMaximumGoogleAllowedOrigins() {
        // GIVEN more than the maximum allowed origins and any destination
        int maximumGoogleCalls = 21;
        final List<LatLong> origins = new ArrayList<>();
        final LatLong sampleLatLong = LatLong.builder().build();
        IntStream.range(0, maximumGoogleCalls).forEach((number) -> origins.add(sampleLatLong));

        // WHEN I call the service to retrive path distance and time
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Google support at most 20 queries at a time");

        // THEN and Exception should be thrown
        googlePostalCodeGateway.getRoutesForMultipleOriginsToSingleDestination(origins, null);
    }

    @Test
    public void shouldThrowGoogleApiExceptionWhenException() {
        // GIVEN a GeoApiContext without credentials
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("invalid-key")
                .build();
        GooglePostalCodeGatewayImpl googlePostalCodeGateway = new GooglePostalCodeGatewayImpl(geoApiContext, new GoogleResponseWrapperToRoute(), meterRegistry);

        // WHEN google throws exception
        expectedException.expect(GoogleApiException.class);

        // THEN GoogleApiException should be thrown
        googlePostalCodeGateway.getMapLocationByPostalCode("0000", "NL", "some-street");
    }
}