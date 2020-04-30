package com.jumbo.routeplanner.gateway.mongo.repository;

import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeID;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PostalCodeRepository extends PagingAndSortingRepository<PostalCodeDocument, PostalCodeID> {
}
