package com.claimwildcats.api.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.domain.UserRole;
import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.repository.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    //?

    @Autowired
    private ItemService itemService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private UserRepository userRepository;


    //main profile logicc
    public UserProfile getProfile (String userId){
        // 1. get stats from other services
        List <ItemSummary> reports = listMyReports(userId);
        long resolvedCount = reports.stream()
                .filter(summary -> summary.status() == ItemStatus.CLAIMED)
                .count();
        long openCount = reports.size() - resolvedCount;

        Optional<User> userEntity = userRepository.findById(userId);

        if (userEntity.isPresent()){
            return mapToUserProfile(userEntity.get(), (int) openCount,(int)resolvedCount);
        } else {
            // fallback default
            return new UserProfile(
                userId,
                userId,
                userId,
                UserRole.USER,
                false,
                (int) openCount,
                (int) resolvedCount,
                Instant.now());
        }
        }
    

        public List<ItemSummary> listMyReports(String userId){
            // has to add listReportsForUser function in ItemService
            return itemService.listReportsForUser(userId); // to make
        }

        public List<ClaimSummary> listMyClaims(String userId){
            return claimService.listClaimsForUser(userId); // to make
        }

        //login ??
        public void ensureUserExists(String userId, String email, String name){
            if (!userRepository.existsById(userId)){ // to make
                User newUser = new User(userId, email);
                newUser.setFullName(name);
                userRepository.save(newUser);
            }
        }

        private UserProfile mapToUserProfile(User user, int openCount, int resolvedCount){
            return new UserProfile(
                user.getId(),
                user.getFullName() != null ? user.getFullName() : user.getId(),
                user.getEmail() != null ? user.getEmail() : user.getId(),
                user.getRole(),
                user.isEmailVerified(),
                openCount,
                resolvedCount,
                user.getCreatedAt()
            );
        }

        public void registerNewUser(String fullName, String email, String password, String roleInput) {
            String newUserId = java.util.UUID.randomUUID().toString();
    
            User newUser = new User(newUserId, email);
            newUser.setFullName(fullName);
            newUser.setPassword(password);  
            try {
                if (roleInput != null && !roleInput.isEmpty()) {
                    newUser.setRole(UserRole.valueOf(roleInput.toUpperCase())); 
                } else {
                    newUser.setRole(UserRole.USER);
                }
            } catch (Exception e) {
                newUser.setRole(UserRole.USER);
            }
                userRepository.save(newUser);
        }

        public User login(String email, String password) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }
    
            User user = userOpt.get();
    
            
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid password");
            }
    
            return user;
        }

        
    }

