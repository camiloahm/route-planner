package com.jumbo.routeplanner.configuration.swagger;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;

import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SpringfoxConfig {

    private final ServletContext servletContext;

    @Bean
    @Profile("!dev")
    public Docket gatewayApi() {
        return new Docket(SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jumbo.routeplanner.gateway.controller"))
                .paths(PathSelectors.regex("/*.*"))
                .build()
                .apiInfo(apiInfo())
                .pathProvider(new RelativePathProvider(servletContext) {
                    @Override
                    public String getApplicationBasePath() {
                        return "/v1/routeplanner/";
                    }
                });
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Jumbo API")
                .description("Routing Operations")
                .build();
    }

    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .displayRequestDuration(true)
                .build();
    }

    @Profile("dev")
    @Bean
    public Docket gatewayApidev() {
        return new Docket(SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jumbo.routeplanner.gateway.controller"))
                .paths(PathSelectors.regex("/*.*"))
                .build()
                .apiInfo(apiInfo());
    }


}
