package com.claimwildcats.api.service;

import com.google.firebase.FirebaseApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class FirebaseFacade {

    private static final Logger log = LoggerFactory.getLogger(FirebaseFacade.class);

    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    public FirebaseFacade(ObjectProvider<FirebaseApp> firebaseAppProvider) {
        this.firebaseAppProvider = firebaseAppProvider;
    }

    public boolean isReady() {
        return firebaseAppProvider.getIfAvailable() != null;
    }

    public FirebaseApp getAppOrThrow() {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            throw new IllegalStateException("FirebaseApp has not been initialized. Check firebase.enabled in configuration.");
        }
        return firebaseApp;
    }

    public void logReadiness() {
        if (isReady()) {
            log.debug("FirebaseApp is configured and ready.");
        } else {
            log.warn("FirebaseApp is not configured; falling back to in-memory stubs.");
        }
    }
}
