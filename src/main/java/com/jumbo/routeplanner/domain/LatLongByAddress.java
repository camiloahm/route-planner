package com.jumbo.routeplanner.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatLongByAddress {

    @NotEmpty
    private String postalCode;

    @NotNull
    private Country countryCode;

    private String street;

    @AssertTrue(message = "Belgium address should have street and be composed of 4 numbers")
    private boolean isBelgiumAddress() {
        if (Country.BE.equals(countryCode)) {
            if (StringUtils.hasText(street)) {
                return postalCode.replaceAll("\\s+", "").matches("[0-9]{4}");
            }
            return false;
        }
        return true;
    }

    @AssertTrue(message = "Netherland address should have 4 numbers and 2 letters")
    private boolean isNetherlandAddress() {
        if (Country.NL.equals(this.countryCode)) {
            if (StringUtils.hasText(postalCode)) {
                return postalCode.replaceAll("\\s+", "").matches("[0-9]{4}[a-zA-Z]{2}");
            }
            return false;
        }
        return true;
    }
}
