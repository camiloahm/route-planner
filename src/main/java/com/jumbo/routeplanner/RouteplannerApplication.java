package com.jumbo.routeplanner;

import com.jumbo.routeplanner.configuration.google.GoogleAPIProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GoogleAPIProperties.class})
public class RouteplannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RouteplannerApplication.class, args);
    }
}
