package com.wotos.wotosplayerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 *
 * <p>Springfox was replaced with SpringDoc OpenAPI during the Spring Boot 3 upgrade.
 * SpringDoc auto-configures the {@code /v3/api-docs} endpoint and Swagger UI at
 * {@code /swagger-ui/index.html}; this bean only supplies the API metadata.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swaggerApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("WoToS Player Service")
                        .description("Looks up and persists World of Tanks player accounts, "
                                + "vehicle stats and achievement snapshots via the WoT API.")
                        .version("v1"));
    }

}
