package com.claimwildcats.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.dto.LoginRequest;
import com.claimwildcats.api.dto.RegisterRequest;
import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
@CrossOrigin(origins = "http://localhost:5173") 
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- NEW REGISTER ENDPOINT ---
    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Register a new user in MySQL")
    public void register(@RequestBody RegisterRequest request) {
        userService.registerNewUser(
            request.getFull_name(), 
            request.getEmail(), 
            request.getPassword(), 
            request.getRole()
        );
    }

    // --- EXISTING ENDPOINTS ---

    @GetMapping("/{userId}")
    @Operation(summary = "User profile", description = "Fetch profile information for a campus community member.")
    public UserProfile profile(@PathVariable String userId) {
        return userService.getProfile(userId);
    }

    @GetMapping("/{userId}/reports")
    @Operation(summary = "User reports", description = "List lost and found reports created by the user.")
    public List<ItemSummary> reports(@PathVariable String userId) {
        return userService.listMyReports(userId);
    }

    @GetMapping("/{userId}/claims")
    @Operation(summary = "User claims", description = "List claims submitted by the user.")
    public List<ClaimSummary> claims(@PathVariable String userId) {
        return userService.listMyClaims(userId);
    }

    @PostMapping("/login")
    @Operation(summary = "Login User", description = "Authenticate user via MySQL")
    public User login(@RequestBody LoginRequest request) {
        return userService.login(request.getEmail(), request.getPassword());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{userId}")
    @Operation(summary = "Update Profile", description = "Update user name or details")
    public void updateProfile(@PathVariable String userId, @RequestBody RegisterRequest request) {
        userService.updateUser(userId, request.getFull_name(), request.getEmail());
    }

    
}