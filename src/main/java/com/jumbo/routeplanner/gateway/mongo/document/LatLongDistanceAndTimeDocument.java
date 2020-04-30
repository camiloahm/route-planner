package com.jumbo.routeplanner.gateway.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
@Document(collection = "LatLongDistanceAndTime")
@CompoundIndexes(
        {
                @CompoundIndex(name = "originAndDestLatAndLong", def = "{'_id.destinationLatitude' : 1, '_id.destinationLongitude' : 1, '_id.originLatitude' : 1, '_id.originLongitude' : 1}", unique = true, background = true),
        }
)
public class LatLongDistanceAndTimeDocument {

    @Id
    private LatLongDistanceAndTimeID distanceTimeID;

    private Double pathDistanceInMeters;
    private Long distanceTimeInSeconds;

    @Indexed(expireAfterSeconds = 2160000, background = true) // Expires after 25 days
    private LocalDateTime createdDate;

}
