package com.claimwildcats.api.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserRole;
import com.claimwildcats.api.dto.RegisterRequest;
import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.entity.UserProfile;
import com.claimwildcats.api.repository.UserProfileRepository;
import com.claimwildcats.api.repository.UserRepository;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private UserProfileRepository profileRepository;


    private static final Pattern ID_PATTERN = Pattern.compile("\\d{2}-\\d{4}-\\d{3}");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^09\\d{9}$");


    // ====================== REGISTER ======================
    public void registerNewUser(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!ID_PATTERN.matcher(req.getIdNumber()).matches()) {
            throw new RuntimeException("Invalid ID number format");
        }

        if (!CONTACT_PATTERN.matcher(req.getContactNumber()).matches()) {
            throw new RuntimeException("Invalid contact number");
        }

        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setIdNumber(req.getIdNumber());
        user.setContactNumber(req.getContactNumber());
        user.setDepartment(req.getDepartment());
        user.setPassword(req.getPassword()); // TODO: hash later
        user.setRole(UserRole.USER);
        user.setCreatedAt(Instant.now());

        //userRepository.save(user);

        User savedUser = userRepository.save(user);

        // Create empty profile
        UserProfile profile = new UserProfile(savedUser);
        profileRepository.save(profile);

    }

    // ====================== LOGIN ======================
    public User login(String identifier, String password) {
        Optional<User> userOpt = userRepository.findByEmailOrUsername(identifier, identifier);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) throw new RuntimeException("Invalid password");

        return user;
    }

    // ====================== PROFILE ======================
    // public UserProfile getProfile(Long userId) {
    // return profileRepository.findByUserUserId(userId)
    //     .orElseThrow(() -> new RuntimeException("Profile not found"));
    // }


    // private UserProfile mapToUserProfile(User user) {
    //     return new UserProfile(
    //             String.valueOf(user.getUserId()),
    //             user.getName(),
    //             user.getEmail(),
    //             user.getRole(),
    //             true,
    //             listMyReports(user.getUserId()).size(),
    //             listMyClaims(user.getUserId()).size(),
    //             user.getCreatedAt()
    //     );
    // }

    // ====================== LIST REPORTS ======================
    public List<ItemSummary> listMyReports(Long userId) {
        return itemService.listReportsForUser(String.valueOf(userId));
    }

    // ====================== LIST CLAIMS ======================
    public List<ClaimSummary> listMyClaims(Long userId) {
        return claimService.listClaimsForUser(String.valueOf(userId));
    }

    // ====================== UPDATE USER ======================
    public void updateUser(Long userId, String newName, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newName != null && !newName.isEmpty()) {
            user.setName(newName);
        }
        if (newEmail != null && !newEmail.isEmpty()) {
            user.setEmail(newEmail);
        }

        userRepository.save(user);
    }

}
