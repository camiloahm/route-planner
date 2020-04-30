package com.jumbo.routeplanner.configuration.app;

import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import com.jumbo.routeplanner.gateway.mongo.MongoDbPostalCodeGatewayDecorator;
import com.jumbo.routeplanner.gateway.mongo.converter.LatLongDistanceAndTimeDocumentToRoute;
import com.jumbo.routeplanner.gateway.mongo.converter.RouteToLatLongDistanceAndTimeDocument;
import com.jumbo.routeplanner.gateway.mongo.repository.LatLongDistanceAndTimeRepository;
import com.jumbo.routeplanner.gateway.mongo.repository.PostalCodeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PostalCodeGatewayConfiguration {
    private final RouteplannerGateway routeplannerGateway;
    private final PostalCodeRepository postalCodeRepository;
    private final LatLongDistanceAndTimeRepository latLongDistanceAndTimeRepository;
    private final LatLongDistanceAndTimeDocumentToRoute latLongDistanceAndTimeDocumentToRoute;
    private final RouteToLatLongDistanceAndTimeDocument routeToLatLongDistanceAndTimeDocument;
    private final MeterRegistry meterRegistry;


    @Autowired
    public PostalCodeGatewayConfiguration(@Qualifier("googlePostalCodeGatewayImpl") RouteplannerGateway routeplannerGateway,
                                          PostalCodeRepository postalCodeRepository,
                                          LatLongDistanceAndTimeRepository latLongDistanceAndTimeRepository,
                                          LatLongDistanceAndTimeDocumentToRoute latLongDistanceAndTimeDocumentToRoute,
                                          RouteToLatLongDistanceAndTimeDocument routeToLatLongDistanceAndTimeDocument,
                                          MeterRegistry meterRegistry) {
        this.routeplannerGateway = routeplannerGateway;
        this.postalCodeRepository = postalCodeRepository;
        this.latLongDistanceAndTimeRepository = latLongDistanceAndTimeRepository;
        this.latLongDistanceAndTimeDocumentToRoute = latLongDistanceAndTimeDocumentToRoute;
        this.routeToLatLongDistanceAndTimeDocument = routeToLatLongDistanceAndTimeDocument;
        this.meterRegistry = meterRegistry;
    }


    @Primary
    @Bean
    public RouteplannerGateway getRouteplannerGateway() {
        return new MongoDbPostalCodeGatewayDecorator(
                routeplannerGateway,
                postalCodeRepository,
                latLongDistanceAndTimeRepository,
                latLongDistanceAndTimeDocumentToRoute,
                routeToLatLongDistanceAndTimeDocument,
                meterRegistry);
    }
}
