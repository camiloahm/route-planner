package com.jumbo.routeplanner.gateway.google.converter;

import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.google.to.GoogleResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GoogleResponseWrapperToRoute implements Converter<GoogleResponseWrapper, List<Route>> {

    @Override
    public List<Route> convert(final GoogleResponseWrapper source) {
        final List<Route> routeList = new ArrayList<>();

        if (hasGoogleReturnedRequiredObjects(source)) {
            DistanceMatrixRow[] rows = source.getDistanceMatrixResponse().rows;
            for (int currentRow = 0; currentRow < rows.length; currentRow++) {
                final DistanceMatrixRow distanceMatrixRow = rows[currentRow];
                final DistanceMatrixElement[] elements = distanceMatrixRow.elements;
                final DistanceMatrixElement distanceMatrixElement = elements[0];
                if (hasGoogleCalculatedDistanceSuccessfully(distanceMatrixElement)) {
                    routeList.add(
                            Route.builder()
                                    .origin(source.getOriginLatAndLongList().get(currentRow))
                                    .destination(source.getDestinationLatAndLong())
                                    .distanceInMeters((double) distanceMatrixElement.distance.inMeters)
                                    .timeInSeconds(distanceMatrixElement.duration.inSeconds)
                                    .build());
                } else {
                    log.error("Google returned a Error Status {} for source lat and long {} and destination lat and long {}",
                            distanceMatrixElement.status, source.getOriginLatAndLongList().get(currentRow), source.getDestinationLatAndLong());
                }
            }
        }
        return routeList;
    }

    private boolean hasGoogleReturnedRequiredObjects(final GoogleResponseWrapper googleResponseWrapper) {

        if (googleResponseWrapper == null) {
            log.error("Google returned no response");
            return false;
        } else if (googleResponseWrapper.getDistanceMatrixResponse() == null) {
            log.error("Google returned no Distance Matrix response {}", googleResponseWrapper);
            return false;
        } else if (googleResponseWrapper.getDistanceMatrixResponse().rows == null || googleResponseWrapper.getDistanceMatrixResponse().rows.length == 0) {
            log.error("Google returned no rows for the Distance Matrix response {}", googleResponseWrapper.getDistanceMatrixResponse());
            return false;
        }
        return true;
    }

    private boolean hasGoogleCalculatedDistanceSuccessfully(DistanceMatrixElement distanceMatrixElement) {
        return distanceMatrixElement.status.equals(DistanceMatrixElementStatus.OK);
    }
}
