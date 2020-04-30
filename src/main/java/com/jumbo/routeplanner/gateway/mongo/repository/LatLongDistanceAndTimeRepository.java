package com.jumbo.routeplanner.gateway.mongo.repository;

import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LatLongDistanceAndTimeRepository extends PagingAndSortingRepository<LatLongDistanceAndTimeDocument, LatLongDistanceAndTimeID> {
}
