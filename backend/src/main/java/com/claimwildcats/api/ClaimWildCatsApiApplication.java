package com.claimwildcats.api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.claimwildcats.api")
public class ClaimWildCatsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimWildCatsApiApplication.class, args);
    }
}
