package it.com.jumbo.routeplanner.gateway.mongo;

import com.jumbo.routeplanner.RouteplannerApplication;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeID;
import it.com.jumbo.routeplanner.configuration.google.GoogleAPIConfigurationIT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {RouteplannerApplication.class, GoogleAPIConfigurationIT.class})
@ActiveProfiles("test")
public class MongoTestIT {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void shouldCreateIndexesForPostalCodeCollection() {

        // WHEN the application start
        // THEN all the mongo indexes should be created successfully with the proper name
        final Set<String> allowedIndexes = new HashSet<>();
        allowedIndexes.add("_id.postalCode_1__id.countryCode_1__id.streetName_1");
        allowedIndexes.add("_id.postalCode_1__id.countryCode_1");
        allowedIndexes.add("_id.postalCode_1");
        allowedIndexes.add("createdDate");
        allowedIndexes.add("_id_");

        List<IndexInfo> postalCodeIndexes = mongoTemplate.indexOps("PostalCode").getIndexInfo();
        then(postalCodeIndexes.size()).isEqualTo(5);
        postalCodeIndexes.forEach(indexInfo -> {
            final String indexInfoName = indexInfo.getName();
            if (!allowedIndexes.contains(indexInfoName)) {
                fail("No index with name " + indexInfoName + " allowed");
            }
        });

        // THEN the related mongo indexes fields should not be changed
        getField(PostalCodeID.builder().build(), "streetName");
        getField(PostalCodeID.builder().build(), "countryCode");
        getField(PostalCodeID.builder().build(), "postalCode");
    }

    @Test
    public void shouldCreateIndexesForLatLongDistanceAndTimeCollection() {

        // WHEN the application start
        // THEN all the mongo indexes should be created successfully with the proper name
        final Set<String> allowedIndexes = new HashSet<>();
        allowedIndexes.add("originAndDestLatAndLong");
        allowedIndexes.add("createdDate");
        allowedIndexes.add("_id_");

        List<IndexInfo> postalCodeIndexes = mongoTemplate.indexOps("LatLongDistanceAndTime").getIndexInfo();
        then(postalCodeIndexes.size()).isEqualTo(3);
        postalCodeIndexes.forEach(indexInfo -> {
            final String indexInfoName = indexInfo.getName();
            if (!allowedIndexes.contains(indexInfoName)) {
                fail("No index with name " + indexInfoName + " allowed");
            }
        });

        // THEN the related mongo indexes fields should not be changed
        getField(LatLongDistanceAndTimeID.builder().build(), "destinationLatitude");
        getField(LatLongDistanceAndTimeID.builder().build(), "destinationLongitude");
        getField(LatLongDistanceAndTimeID.builder().build(), "originLatitude");
        getField(LatLongDistanceAndTimeID.builder().build(), "originLongitude");
    }
}
