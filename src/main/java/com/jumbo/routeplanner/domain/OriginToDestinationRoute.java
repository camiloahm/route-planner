package com.jumbo.routeplanner.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OriginToDestinationRoute {
    @NotNull
    @Valid
    private LatLong origin;
    @NotNull
    @Valid
    private LatLong destination;
}
