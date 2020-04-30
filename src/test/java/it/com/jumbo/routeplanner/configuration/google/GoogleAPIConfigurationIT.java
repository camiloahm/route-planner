package it.com.jumbo.routeplanner.configuration.google;

import com.google.maps.GeoApiContext;
import com.jumbo.routeplanner.configuration.google.GoogleAPIProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

@Configuration
@RequiredArgsConstructor
public class GoogleAPIConfigurationIT {

    private final GoogleAPIProperties googleAPIProperties;

    @Value("${app.mockedServer.url}")
    private String mockedServerURL;

    @Value("${app.mockedServer.port}")
    private int mockedServerPort;

    @Bean
    @Primary
    public GeoApiContext geoApiContext() {
        final String wiremockHost = mockedServerURL + ":" + mockedServerPort;

        final GeoApiContext.Builder builder = new GeoApiContext.Builder()
                .maxRetries(googleAPIProperties.getMaxRetries())
                .queryRateLimit(googleAPIProperties.getQueryRateLimit())
                .connectTimeout(googleAPIProperties.getTimeoutInSeconds(), TimeUnit.SECONDS)
                .apiKey(googleAPIProperties.getApiKey());
        invokeMethod(builder, "baseUrlOverride", wiremockHost);
        return builder.build();
    }

}
