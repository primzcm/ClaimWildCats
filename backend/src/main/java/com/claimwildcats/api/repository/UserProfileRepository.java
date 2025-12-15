package com.claimwildcats.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.claimwildcats.api.entity.UserProfile;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserUserId(Long userId);
}
