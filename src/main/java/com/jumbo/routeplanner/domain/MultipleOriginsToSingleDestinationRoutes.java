package com.jumbo.routeplanner.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ApiModel(description = "Input data for many origins to the same destinations")
public class MultipleOriginsToSingleDestinationRoutes {

    @NotEmpty
    @Size(min = 1, max = 20)
    @Valid
    private List<LatLong> origins;

    @NotNull
    @Valid
    private LatLong destination;

}
