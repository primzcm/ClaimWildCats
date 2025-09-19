package com.claimwildcats.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI claimWildCatsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClaimWildCats API")
                        .version("v0.1.0")
                        .description("Lost & Found platform for campus operations.")
                        .contact(new Contact().name("ClaimWildCats Team").email("support@claimwildcats.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url("/")));
    }
}
