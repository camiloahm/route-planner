package com.jumbo.routeplanner.configuration.google;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app.google")
public class GoogleAPIProperties {

    private int maxRetries;
    private int queryRateLimit;
    private int timeoutInSeconds;
    private String apiKey;
}
