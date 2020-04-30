package com.jumbo.routeplanner.gateway;

import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.Route;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RouteplannerGateway {

    Optional<LatLong> getMapLocationByPostalCode(String postalCode, String countryCode, String street);

    Collection<Route> getRoutesForMultipleOriginsToSingleDestination(final List<LatLong> origins, final LatLong destination);
}


