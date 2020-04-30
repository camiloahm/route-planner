package com.jumbo.routeplanner.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LatLong {
    @NotNull
    @Range(min = -90, max = 90)
    @ApiModelProperty(value = "A latitude", example = "51.44149729999999")
    private Double lat;

    @NotNull
    @Range(min = -180, max = 180)
    @ApiModelProperty(value = "A longitude", example = "5.4508784")
    private Double lng;
}
