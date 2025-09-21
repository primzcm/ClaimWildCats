package com.claimwildcats.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemStatus {
    LOST("lost"),
    FOUND("found"),
    CLAIMED("claimed");

    private final String jsonValue;

    ItemStatus(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    public String storageValue() {
        return name();
    }

    @JsonCreator
    public static ItemStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalised = value.trim().toLowerCase();
        for (ItemStatus status : values()) {
            if (status.jsonValue.equals(normalised)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown item status: " + value);
    }
}
