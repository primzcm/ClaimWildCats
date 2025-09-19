package com.claimwildcats.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private final FirebaseProperties properties;
    private final ResourceLoader resourceLoader;

    public FirebaseConfig(FirebaseProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @Bean
    @ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        FirebaseProperties.Credentials credentials = properties.getCredentials();
        GoogleCredentials googleCredentials = loadCredentials(credentials);
        builder.setCredentials(googleCredentials);

        if (properties.getProjectId() != null) {
            builder.setProjectId(properties.getProjectId());
        }
        if (properties.getDatabaseUrl() != null) {
            builder.setDatabaseUrl(properties.getDatabaseUrl());
        }
        if (properties.getStorageBucket() != null) {
            builder.setStorageBucket(properties.getStorageBucket());
        }

        log.info("Initializing FirebaseApp for project {}", properties.getProjectId());
        return FirebaseApp.initializeApp(builder.build());
    }

    private GoogleCredentials loadCredentials(FirebaseProperties.Credentials credentials) throws IOException {
        String location = credentials.getLocation();
        if (location != null && !location.isBlank()) {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Unable to locate Firebase credentials at " + location);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        log.warn("Falling back to Google default credentials for Firebase initialization.");
        return GoogleCredentials.getApplicationDefault();
    }
}
