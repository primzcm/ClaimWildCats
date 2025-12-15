package com.claimwildcats.api.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.entity.UserProfile;
import com.claimwildcats.api.repository.UserProfileRepository;
import com.claimwildcats.api.repository.UserRepository;



@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class UserProfileController {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    public UserProfileController(
        UserRepository userRepository,
        UserProfileRepository profileRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    UserProfile profile = profileRepository
        .findByUserUserId(userId)
        .orElseGet(() -> {
            UserProfile newProfile = new UserProfile(user);
            return profileRepository.save(newProfile);
        });

    return ResponseEntity.ok(profile);
}

  @PostMapping("/{userId}/profile-image")
        public ResponseEntity<UserProfile> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
        ) throws IOException {

    UserProfile profile = profileRepository
        .findByUserUserId(userId)
        .orElseThrow();

    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path path = Paths.get("uploads", filename);
    Files.createDirectories(path.getParent());
    Files.write(path, file.getBytes());

    profile.setProfileImageUrl("/uploads/" + filename);
    profileRepository.save(profile);

    return ResponseEntity.ok(profile);
}

@PostMapping("/{userId}/cover-image")
    public ResponseEntity<UserProfile> uploadCoverImage(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {

    UserProfile profile = profileRepository
        .findByUserUserId(userId)
        .orElseThrow();

    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path path = Paths.get("uploads", filename);
    Files.createDirectories(path.getParent());
    Files.write(path, file.getBytes());

    profile.setCoverImageUrl("/uploads/" + filename);
    profileRepository.save(profile);

    return ResponseEntity.ok(profile);
}


@DeleteMapping("/{userId}/profile-image")
public ResponseEntity<?> removeProfileImage(@PathVariable Long userId) {
    UserProfile profile = profileRepository
        .findByUserUserId(userId)
        .orElseThrow();

    profile.setProfileImageUrl(null);
    profileRepository.save(profile);

    return ResponseEntity.ok().build();
}


}

