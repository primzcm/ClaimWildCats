package com.claimwildcats.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
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
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    userService.registerNewUser(request);
    return ResponseEntity.ok().build();
}


    // --- EXISTING ENDPOINTS ---
    //Removed for the mean time
    // @GetMapping("/{userId}")
    // @Operation(summary = "User profile", description = "Fetch profile information for a campus community member.")
    // public UserProfile profile(@PathVariable Long userId) {
    //     return userService.getProfile(userId);
    // }

    @GetMapping("/{userId}/reports")
    @Operation(summary = "User reports", description = "List lost and found reports created by the user.")
    public List<ItemSummary> reports(@PathVariable Long userId) {
        return userService.listMyReports(userId);
    }

    @GetMapping("/{userId}/claims")
    @Operation(summary = "User claims", description = "List claims submitted by the user.")
    public List<ClaimSummary> claims(@PathVariable Long userId) {
        return userService.listMyClaims(userId);
    }

    @PostMapping("/login")
public ResponseEntity<User> login(@RequestBody LoginRequest request) {
    User user = userService.login(request.getIdentifier(), request.getPassword());
    return ResponseEntity.ok(user);
}


    @org.springframework.web.bind.annotation.PutMapping("/{userId}")
    @Operation(summary = "Update Profile", description = "Update user name or details")
    public void updateProfile(@PathVariable Long userId, @RequestBody RegisterRequest request) {
        userService.updateUser(userId, request.getName(), request.getEmail());
    }

    
}