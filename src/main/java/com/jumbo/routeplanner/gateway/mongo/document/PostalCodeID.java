package com.jumbo.routeplanner.gateway.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class PostalCodeID implements Serializable {
    private String streetName;
    private String countryCode;
    private String postalCode;
}
