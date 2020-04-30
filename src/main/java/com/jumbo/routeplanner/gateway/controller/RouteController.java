package com.jumbo.routeplanner.gateway.controller;


import com.jumbo.routeplanner.domain.MultipleOriginsToSingleDestinationRoutes;
import com.jumbo.routeplanner.domain.OriginToDestinationRoute;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.usecase.UseCase;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.notFound;

@RestController
@RequiredArgsConstructor
public class RouteController {
    private final UseCase<OriginToDestinationRoute, Optional<Route>> getOriginToDestinationRoute;
    private final UseCase<MultipleOriginsToSingleDestinationRoutes, Collection<Route>> getMultipleOriginsToSingleDestinationRoutes;

    @GetMapping(value = "routes")
    @ApiOperation("Retrieve route information")
    @ApiResponses({
            @ApiResponse(code = 200, message = "API called successfully.", response = Route.class),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    public ResponseEntity<Route> getRoute(@Valid OriginToDestinationRoute query) {
        return getOriginToDestinationRoute.execute(query)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @PostMapping(value = "routes")
    @ApiOperation("Retrieve route information")
    @ApiResponses({
            @ApiResponse(code = 200, message = "API called successfully.", response = Route.class),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    public ResponseEntity<Collection<Route>> getRouteForManyOriginsAndSingleDestination(@RequestBody @Valid MultipleOriginsToSingleDestinationRoutes query) {
        final Collection<Route> latAndLongTimeDistances = getMultipleOriginsToSingleDestinationRoutes.execute(query);
        if (latAndLongTimeDistances.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(latAndLongTimeDistances);

    }
}