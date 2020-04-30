package com.jumbo.routeplanner.gateway.mongo.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "PostalCode")
@CompoundIndexes(
        {
                @CompoundIndex(def = "{'_id.postalCode' : 1}", background = true),
                @CompoundIndex(def = "{'_id.postalCode' : 1, '_id.countryCode' : 1}", background = true),
                @CompoundIndex(def = "{'_id.postalCode' : 1, '_id.countryCode' : 1, '_id.streetName' : 1}", unique = true, sparse = true, background = true),
        }
)
@Builder
public class PostalCodeDocument {

    @Id
    private PostalCodeID postalCodeID;

    private Double latitude;
    private Double longitude;

    @Indexed(expireAfterSeconds = 2160000, background = true) // Expires after 25 days
    private LocalDateTime createdDate;
}