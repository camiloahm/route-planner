package com.jumbo.routeplanner.configuration.google;

import com.google.maps.GeoApiContext;
import com.google.maps.OkHttpRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.Assert.hasText;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GoogleAPIConfiguration {

    private final GoogleAPIProperties googleAPIProperties;
    private final List<Interceptor> interceptors;

    @Bean
    @Profile("!test")
    public GeoApiContext geoApiContext() {
        log.info("Configuring API Key Logging for Google API");
        hasText(googleAPIProperties.getApiKey(), "For API key login with google, the API Key must be provided");

        OkHttpRequestHandler.Builder builder = new OkHttpRequestHandler.Builder();
        interceptors.forEach(builder.okHttpClientBuilder()::addInterceptor);

        return new GeoApiContext.Builder()
                .requestHandlerBuilder(builder)
                .maxRetries(googleAPIProperties.getMaxRetries())
                .queryRateLimit(googleAPIProperties.getQueryRateLimit())
                .connectTimeout(googleAPIProperties.getTimeoutInSeconds(), TimeUnit.SECONDS)
                .apiKey(googleAPIProperties.getApiKey())
                .build();
    }
}

