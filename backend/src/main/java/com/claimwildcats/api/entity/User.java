package com.claimwildcats.api.entity;

import java.time.Instant;

import com.claimwildcats.api.domain.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;
    private String fullName;
    private String email;
    private boolean emailVerified;


    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Instant createdAt;

    public User(){
        this.createdAt = Instant.now();
        this.role = UserRole.USER;
    }

    public User (String id, String email){
        this.id = id;
        this.email = email;
        this.createdAt = Instant.now();
        this.role = UserRole.USER;
    }

    //getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    
}


