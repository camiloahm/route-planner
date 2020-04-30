package com.jumbo.routeplanner.gateway.mongo.converter;

import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LatLongDistanceAndTimeDocumentToRoute implements Converter<LatLongDistanceAndTimeDocument, Route> {
    @Override
    public Route convert(final LatLongDistanceAndTimeDocument source) {
        LatLongDistanceAndTimeID id = source.getDistanceTimeID();
        return Route.builder()
                .destination(LatLong.builder()
                        .lng(id.getDestinationLongitude())
                        .lat(id.getDestinationLatitude())
                        .build())
                .origin(LatLong.builder()
                        .lng(id.getOriginLongitude())
                        .lat(id.getOriginLatitude())
                        .build())
                .distanceInMeters(source.getPathDistanceInMeters())
                .timeInSeconds(source.getDistanceTimeInSeconds())
                .build();
    }
}
