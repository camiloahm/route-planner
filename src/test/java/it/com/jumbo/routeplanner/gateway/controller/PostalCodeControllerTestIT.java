package it.com.jumbo.routeplanner.gateway.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jumbo.routeplanner.RouteplannerApplication;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.PostalCodeID;
import it.com.jumbo.routeplanner.configuration.google.GoogleAPIConfigurationIT;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {RouteplannerApplication.class, GoogleAPIConfigurationIT.class} )
@ActiveProfiles("test")
public class PostalCodeControllerTestIT {

    private static final String POSTALCODE_URL = "/postalcodes?countryCode={countryCode}&postalCode={postalCode}";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8543);

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setupTest() {
        // The application will not be allowed to make table scan
        mongoTemplate.scriptOps().execute(new ExecutableMongoScript("db.adminCommand( { setParameter: 1, notablescan: 1 } )")); // Disable table scan
        mongoTemplate.remove(new Query(), "PostalCode");
        wireMockRule.resetMappings();
    }

    @Test
    public void shouldRetrieveLatitudeAndLongitudeGivenNLPostalCodeFromGoogleAndStoreItInMongo() throws IOException {
        // GIVEN a valid Input Postal Code with Country
        final Map given = new HashMap<String, String>();
        given.put("countryCode", "NL");
        given.put("postalCode", "  5616 VD   ");

        wireMockRule
                .stubFor(get(urlEqualTo("/maps/api/geocode/json?key=AIza...&components=country%3ANL%7Cpostal_code%3A5616VD"))
                        .willReturn(aResponse()
                                .withBody(getJSONResponse("success-getpostalcode.json"))
                                .withStatus(OK.value())));

        // WHEN I try to get the lat and log
        final ResponseEntity actual =
                testRestTemplate.getForEntity(POSTALCODE_URL, LatLong.class, given);

        // THEN the returned data should be the following
        final LatLong expectedLatLong = LatLong.builder().lat(51.4421179).lng(5.4497397).build();
        then(actual.getBody()).isEqualToComparingFieldByField(expectedLatLong);
        then(actual.getStatusCode()).isEqualTo(OK);

        // THEN the data should've being correctly stored in Mongo
        PostalCodeID expectedPostalCodeID = PostalCodeID.builder()
                .countryCode("NL")
                .postalCode("5616VD")
                .build();
        PostalCodeDocument expectedPostalCodeDocument = PostalCodeDocument.builder()
                .latitude(51.4421179)
                .longitude(5.4497397)
                .postalCodeID(expectedPostalCodeID)
                .build();

        List<PostalCodeDocument> postalCodeDocuments = mongoTemplate.findAll(PostalCodeDocument.class, "PostalCode");
        then(postalCodeDocuments.size()).isEqualTo(1);
        then(postalCodeDocuments.get(0).getPostalCodeID()).isEqualToComparingFieldByField(expectedPostalCodeID);
        then(postalCodeDocuments.get(0).getLatitude()).isEqualTo(expectedPostalCodeDocument.getLatitude());
        then(postalCodeDocuments.get(0).getLongitude()).isEqualTo(expectedPostalCodeDocument.getLongitude());
        then(postalCodeDocuments.get(0).getPostalCodeID().getStreetName()).isNull();
        then(postalCodeDocuments.get(0).getCreatedDate()).isNotNull();
    }

    @Test
    public void shouldRetrieveLatitudeAndLongitudeGivenBelgiumPostalCodeFromGoogleAndStoreItInMongo() throws IOException {
        // GIVEN a valid Input Postal Code with Country
        final Map given = new HashMap<String, String>();
        given.put("countryCode", "BE");
        given.put("postalCode", "3900 ");
        given.put("street", "Grachtstraat 3-1");

        wireMockRule
                .stubFor(get(urlEqualTo("/maps/api/geocode/json?key=AIza...&components=country%3ABE%7Cpostal_code%3A3900&address=GRACHTSTRAAT+3-1"))
                        .willReturn(aResponse()
                                .withBody(getJSONResponse("success-getpostalcode-be.json"))
                                .withStatus(OK.value())));

        // WHEN I try to get the lat and log
        final ResponseEntity actual =
                testRestTemplate.getForEntity("/postalcodes?countryCode={countryCode}&postalCode={postalCode}&street={street}", LatLong.class, given);

        // THEN the returned data should be the following
        final LatLong expectedLatLong = LatLong.builder().lat(51.2070619).lng(5.4101399).build();
        then(actual.getBody()).isEqualToComparingFieldByField(expectedLatLong);
        then(actual.getStatusCode()).isEqualTo(OK);

        // THEN the data should've being correctly stored in Mongo
        PostalCodeID expectedPostalCodeID = PostalCodeID.builder()
                .countryCode("BE")
                .postalCode("3900")
                .streetName("GRACHTSTRAAT 3-1")
                .build();
        PostalCodeDocument expectedPostalCodeDocument = PostalCodeDocument.builder()
                .latitude(51.2070619)
                .longitude(5.4101399)
                .postalCodeID(expectedPostalCodeID)
                .build();

        List<PostalCodeDocument> postalCodeDocuments = mongoTemplate.findAll(PostalCodeDocument.class, "PostalCode");
        then(postalCodeDocuments.size()).isEqualTo(1);
        then(postalCodeDocuments.get(0).getPostalCodeID()).isEqualToComparingFieldByField(expectedPostalCodeID);
        then(postalCodeDocuments.get(0).getLatitude()).isEqualTo(expectedPostalCodeDocument.getLatitude());
        then(postalCodeDocuments.get(0).getLongitude()).isEqualTo(expectedPostalCodeDocument.getLongitude());
        then(postalCodeDocuments.get(0).getCreatedDate()).isNotNull();
    }

    @Test
    public void shouldRetrieveLatitudeAndLongitudeFromMongoWithoutReachingGoogle() {
        // GIVEN a valid Input Postal Code with Country that is already in Cache
        final Map given = new HashMap<String, String>();
        given.put("countryCode", "NL");
        given.put("postalCode", "  5617 VD   ");
        PostalCodeID givenPostalCodeID = PostalCodeID.builder()
                .countryCode("NL")
                .postalCode("5617VD")
                .build();
        final PostalCodeDocument givenPostalCodeDocument = PostalCodeDocument.builder()
                .latitude(51.6521179)
                .longitude(5.3597397)
                .postalCodeID(givenPostalCodeID)
                .build();
        mongoTemplate.save(givenPostalCodeDocument);

        // WHEN I try to get the lat and log (turning of wiremock should not cause exceptions)
        wireMockRule.stop();
        final ResponseEntity actual =
                testRestTemplate.getForEntity(POSTALCODE_URL, LatLong.class, given);

        // THEN it should returned mongo Data
        final LatLong expectedLatLong = LatLong.builder().lat(51.6521179).lng(5.3597397).build();
        then(actual.getBody()).isEqualToComparingFieldByField(expectedLatLong);
        then(actual.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldStoreLatitudeAndLongitudeInMongoEvenIfNotFoundInFromGoogle() throws IOException {
        // GIVEN a valid Input Postal Code with Country
        final Map given = new HashMap<String, String>();
        given.put("countryCode", "NL");
        given.put("postalCode", "  9191 ZZ  ");

        wireMockRule
                .stubFor(get(urlEqualTo("/maps/api/geocode/json?key=AIza...&components=country%3ANL%7Cpostal_code%3A9191ZZ"))
                        .willReturn(aResponse()
                                .withBody(getJSONResponse("not-found-getpostalcode.json"))
                                .withStatus(OK.value())));

        // WHEN I try to get the lat and log
        final ResponseEntity actual =
                testRestTemplate.getForEntity(POSTALCODE_URL, LatLong.class, given);

        // THEN the returned data should be the following
        then(actual.getStatusCode()).isEqualTo(NOT_FOUND);

        // THEN the data should've being correctly stored in Mongo
        PostalCodeID expectedPostalCodeID = PostalCodeID.builder()
                .countryCode("NL")
                .postalCode("9191ZZ")
                .build();

        final PostalCodeDocument actualDocument = mongoTemplate.findById(expectedPostalCodeID, PostalCodeDocument.class, "PostalCode");
        then(actualDocument.getPostalCodeID()).isEqualToComparingFieldByField(expectedPostalCodeID);
        then(actualDocument.getLatitude()).isNull();
        then(actualDocument.getLongitude()).isNull();
        then(actualDocument.getPostalCodeID().getStreetName()).isNull();
        then(actualDocument.getCreatedDate()).isNotNull();
    }

    private String getJSONResponse(final String jsonFileName) throws IOException {
        try (final InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("data/" + jsonFileName)) {
            try (final Scanner scanner = new Scanner(jsonInputStream, "UTF-8")) {
                return scanner.useDelimiter("\\Z").next();
            }
        }
    }
}