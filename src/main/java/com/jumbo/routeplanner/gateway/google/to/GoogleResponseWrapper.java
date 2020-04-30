package com.jumbo.routeplanner.gateway.google.to;

import com.google.maps.model.DistanceMatrix;
import com.jumbo.routeplanner.domain.LatLong;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class GoogleResponseWrapper {

    private final DistanceMatrix distanceMatrixResponse;
    private final LatLong destinationLatAndLong;
    private final List<LatLong> originLatAndLongList;

    public List<LatLong> getOriginLatAndLongList() {
        return Collections.unmodifiableList(originLatAndLongList);
    }


}
