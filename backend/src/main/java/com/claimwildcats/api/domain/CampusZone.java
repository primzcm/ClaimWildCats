package com.claimwildcats.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum CampusZone {
    MAIN("Main"),
    LIBRARY("Library"),
    GYM("Gym"),
    LABS("Labs"),
    CANTEEN("Canteen"),
    PARKING("Parking"),
    GATE1("Gate1"),
    GATE2("Gate2"),
    OTHER("Other");

    private final String jsonValue;

    CampusZone(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static CampusZone fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(zone -> zone.jsonValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown campus zone: " + value));
    }
}
