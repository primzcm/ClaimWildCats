package com.claimwildcats.api.dto;

public class LoginRequest {
    private String identifier; // can be email or username
    private String password;

    // Getter and setter for identifier
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // Getter and setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
