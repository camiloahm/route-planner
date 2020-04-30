package com.jumbo.routeplanner.usecase;

import com.jumbo.routeplanner.domain.OriginToDestinationRoute;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GetOriginToDestinationRoute implements UseCase<OriginToDestinationRoute, Optional<Route>> {

    private final RouteplannerGateway routeplannerGateway;

    @Override
    public Optional<Route> execute(final OriginToDestinationRoute query) {
        Collection<Route> results = routeplannerGateway.getRoutesForMultipleOriginsToSingleDestination(
                Collections.singletonList(query.getOrigin()),
                query.getDestination());

        return results.stream().findFirst();
    }
}
