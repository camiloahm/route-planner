package com.jumbo.routeplanner.usecase;


import com.jumbo.routeplanner.domain.MultipleOriginsToSingleDestinationRoutes;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class GetMultipleOriginsToSingleDestinationRoutes implements UseCase<MultipleOriginsToSingleDestinationRoutes, Collection<Route>> {

    private final RouteplannerGateway routeplannerGateway;

    @Override
    public Collection<Route> execute(final MultipleOriginsToSingleDestinationRoutes query) {
        return routeplannerGateway.getRoutesForMultipleOriginsToSingleDestination(
                query.getOrigins(),
                query.getDestination());
    }
}
