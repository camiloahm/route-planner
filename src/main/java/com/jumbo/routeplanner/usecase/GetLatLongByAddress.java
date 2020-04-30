package com.jumbo.routeplanner.usecase;

import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.LatLongByAddress;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GetLatLongByAddress implements UseCase<LatLongByAddress, Optional<LatLong>> {

    private final RouteplannerGateway postalCodeGateway;

    @Override
    public Optional<LatLong> execute(final LatLongByAddress query) {
        final LatLongByAddress sanitizedQuery = sanitize(query);

        return postalCodeGateway.getMapLocationByPostalCode(
                sanitizedQuery.getPostalCode(),
                sanitizedQuery.getCountryCode().name(),
                sanitizedQuery.getStreet());
    }

    private LatLongByAddress sanitize(LatLongByAddress query) {
        return LatLongByAddress.builder()
                .countryCode(query.getCountryCode())
                .postalCode(normalizePostalCode(query.getPostalCode()))
                .street(normalizeStreet(query.getStreet()))
                .build();
    }

    private String normalizeStreet(final String streetName) {
        if (StringUtils.hasText(streetName)) {
            return streetName.trim().toUpperCase();
        }
        return streetName;
    }

    private String normalizePostalCode(final String postalCode) {
        return postalCode.replaceAll("\\s+", "").toUpperCase();
    }
}
