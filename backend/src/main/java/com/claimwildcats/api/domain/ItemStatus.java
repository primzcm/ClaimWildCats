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
        String trimmed = value.trim();
        for (ItemStatus status : values()) {
            if (status.jsonValue.equalsIgnoreCase(trimmed) || status.name().equalsIgnoreCase(trimmed)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown item status: " + value);
    }
}
