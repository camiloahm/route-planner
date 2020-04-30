package com.jumbo.routeplanner.gateway.mongo.converter;

import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RouteToLatLongDistanceAndTimeDocument implements Converter<Route, LatLongDistanceAndTimeDocument> {

    @Override
    public LatLongDistanceAndTimeDocument convert(final Route source) {
        return LatLongDistanceAndTimeDocument.builder()
                .distanceTimeID(getDistanceTimeID(source))
                .distanceTimeInSeconds(source.getTimeInSeconds())
                .pathDistanceInMeters(source.getDistanceInMeters())
                .build();
    }

    private LatLongDistanceAndTimeID getDistanceTimeID(final Route source) {
        return LatLongDistanceAndTimeID.builder()
                .destinationLongitude(source.getDestination().getLng())
                .destinationLatitude(source.getDestination().getLat())
                .originLatitude(source.getOrigin().getLat())
                .originLongitude(source.getOrigin().getLng())
                .build();
    }
}
