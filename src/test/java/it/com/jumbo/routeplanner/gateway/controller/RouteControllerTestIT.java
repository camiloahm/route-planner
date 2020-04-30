package it.com.jumbo.routeplanner.gateway.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jumbo.routeplanner.RouteplannerApplication;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.document.LatLongDistanceAndTimeID;
import it.com.jumbo.routeplanner.configuration.google.GoogleAPIConfigurationIT;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {RouteplannerApplication.class, GoogleAPIConfigurationIT.class})
@ActiveProfiles("test")
public class RouteControllerTestIT {

    private static final String ROUTES_URL = "/routes?destination.lat={destination.lat}&destination.lng={destination.lng}&origin.lat={origin.lat}&origin.lng={origin.lng}";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8543);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setupTest() {
        // The application will not be allowed to make table scan
        mongoTemplate.scriptOps().execute(new ExecutableMongoScript("db.adminCommand( { setParameter: 1, notablescan: 1 } )")); // Disable table scan
        mongoTemplate.remove(new Query(), "LatLongDistanceAndTime");
        wireMockRule.resetMappings();
    }

    @Test
    public void shouldRetrieveSingleRouteFromGoogleAndStoreItInMongo() throws IOException {
        // GIVEN a valid Input Postal Code with Country
        final Map<String, String> given = new HashMap<>();
        given.put("destination.lat", "53.102946");
        given.put("destination.lng", "5.621201699999999");
        given.put("origin.lat", "53.097019");
        given.put("origin.lng", "5.614660799999999");

        wireMockRule
                .stubFor(get(urlEqualTo("/maps/api/distancematrix/json" +
                        "?key=AIza..." +
                        "&destinations=53.10294600%2C5.62120170" +
                        "&origins=53.09701900%2C5.61466080"))
                        .willReturn(aResponse()
                                .withBody(getJSONFile("data/success-distancematrix-single.json"))
                                .withStatus(OK.value())));

        // WHEN I try to get the route
        final ResponseEntity actual =
                testRestTemplate.getForEntity(ROUTES_URL, Route.class, given);

        // THEN the returned data should be the following
        final Route expectedRoute = Route.builder()
                .timeInSeconds(239L)
                .distanceInMeters(1266.0)
                .origin(LatLong.builder().lat(53.097019).lng(5.614660799999999).build())
                .destination(LatLong.builder().lat(53.102946).lng(5.621201699999999).build())
                .build();

        then(actual.getBody()).isEqualToComparingFieldByField(expectedRoute);
        then(actual.getStatusCode()).isEqualTo(OK);

        // THEN the data should've being correctly stored in Mongo
        LatLongDistanceAndTimeID expectedLatLongDistanceAndTimeID = LatLongDistanceAndTimeID.builder()
                .originLongitude(5.614660799999999)
                .originLatitude(53.097019)
                .destinationLatitude(53.102946)
                .destinationLongitude(5.621201699999999)
                .build();

        final List<LatLongDistanceAndTimeDocument> actualLatLongDistanceAndTimeDocuments
                = mongoTemplate.findAll(LatLongDistanceAndTimeDocument.class, "LatLongDistanceAndTime");
        then(actualLatLongDistanceAndTimeDocuments.size()).isEqualTo(1);
        then(actualLatLongDistanceAndTimeDocuments.get(0).getDistanceTimeID()).isEqualToComparingFieldByField(expectedLatLongDistanceAndTimeID);
        then(actualLatLongDistanceAndTimeDocuments.get(0).getCreatedDate()).isNotNull();
        then(actualLatLongDistanceAndTimeDocuments.get(0).getDistanceTimeInSeconds()).isEqualTo(239L);
        then(actualLatLongDistanceAndTimeDocuments.get(0).getPathDistanceInMeters()).isEqualTo(1266.0);
        then(actualLatLongDistanceAndTimeDocuments.get(0).getCreatedDate()).isNotNull();
    }

    @Test
    public void shouldRetrieveMultipleRoutesGivenMultipleOriginsFromGoogleAndStoreItInMongo() throws Exception {
        // GIVEN a valid Input Postal Code with Country
        final String givenMultipleOriginsSingleDestination = getJSONFile("data/given-multiple-origins-routes.json");

        wireMockRule
                .stubFor(get(urlEqualTo("/maps/api/distancematrix/json" +
                        "?key=AIza..." +
                        "&destinations=51.44155600%2C5.45036100" +
                        "&origins=51.44632500%2C5.45327000%7C51.44170100%2C5.48041200%7C51.44009100%2C5.48041200%7C51.43170400%2C5.48537900%7C51.44823600%2C5.44837400%7C61.27104400%2C11.30165300%7C66.40538900%2C59.10824800"))
                        .willReturn(aResponse()
                                .withBody(getJSONFile("data/success-distancematrix-multiple.json"))
                                .withStatus(OK.value())));

        // WHEN I try to get the route
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(givenMultipleOriginsSingleDestination, headers);

        final ResponseEntity<Collection<Route>> actual = testRestTemplate
                .exchange("/routes", POST, httpEntity, new ParameterizedTypeReference<Collection<Route>>() {
                });

        // THEN the returned data should be the following
        final String expectedRoutes = getJSONFile("data/expected-multiple-routes.json");

        JSONAssert.assertEquals(expectedRoutes, objectMapper.writeValueAsString(actual.getBody()), JSONCompareMode.STRICT);
        then(actual.getStatusCode()).isEqualTo(OK);
        final Collection<Route> actualRoutes = fromJSON(new TypeReference<Collection<Route>>() {
        }, expectedRoutes);
        then(actualRoutes.size()).isEqualTo(6);

        // THEN the data should've being correctly stored in Mongo
        final List<LatLongDistanceAndTimeDocument> actualLatLongDistanceAndTimeDocuments
                = mongoTemplate.findAll(LatLongDistanceAndTimeDocument.class, "LatLongDistanceAndTime");

        final Optional<LatLongDistanceAndTimeDocument> anyNullCreationDate =
                actualLatLongDistanceAndTimeDocuments.stream().filter(findAnyNullCreationDate()).findAny();

        // THEN all the creation date in mongo should be populated
        then(anyNullCreationDate).isEmpty();

        // THEN the expected data saved in mongo should be
        final String expectedMongoDocuments = getJSONFile("data/expected-mongo-multiple.json");
        JSONAssert.assertEquals(expectedMongoDocuments, objectMapper.writeValueAsString(actualLatLongDistanceAndTimeDocuments), JSONCompareMode.LENIENT);


    }

    private String getJSONFile(final String jsonFileName) throws IOException {
        try (final InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream(jsonFileName)) {
            try (final Scanner scanner = new Scanner(jsonInputStream, "UTF-8")) {
                return scanner.useDelimiter("\\Z").next();
            }
        }
    }

    private Predicate<LatLongDistanceAndTimeDocument> findAnyNullCreationDate() {
        return latLongDistanceAndTimeDocument ->
                latLongDistanceAndTimeDocument.getCreatedDate() == null;
    }

    private <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
        T data;
        try {
            data = objectMapper.readValue(jsonPacket, type);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return data;
    }
}