package com.claimwildcats.api;

import com.claimwildcats.api.config.FirebaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FirebaseProperties.class)
public class ClaimWildCatsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimWildCatsApiApplication.class, args);
    }
}
