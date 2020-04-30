package com.jumbo.routeplanner.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private LatLong origin;
    private LatLong destination;
    private Long timeInSeconds;
    private Double distanceInMeters;
}
