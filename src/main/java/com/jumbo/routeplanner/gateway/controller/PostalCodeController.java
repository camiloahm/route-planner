package com.jumbo.routeplanner.gateway.controller;

import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.LatLongByAddress;
import com.jumbo.routeplanner.usecase.UseCase;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.notFound;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PostalCodeController {

    private final UseCase<LatLongByAddress, Optional<LatLong>> getLatLongByAddress;

    @GetMapping(value = "postalcodes")
    @ApiOperation("Retrieve information about the given postal code")
    @ApiResponses({
            @ApiResponse(code = 200, message = "API called successfully.", response = LatLong.class),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    public ResponseEntity<LatLong> getPostalCode(@Valid LatLongByAddress query) {
        return getLatLongByAddress.execute(query)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }
}
