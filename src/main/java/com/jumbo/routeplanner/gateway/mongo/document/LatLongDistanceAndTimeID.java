package com.jumbo.routeplanner.gateway.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class LatLongDistanceAndTimeID implements Serializable {
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double originLatitude;
    private Double originLongitude;
}
