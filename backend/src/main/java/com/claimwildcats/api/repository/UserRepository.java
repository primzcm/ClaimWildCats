package com.claimwildcats.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.claimwildcats.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // we get findById() and save() for free !
    Optional<User> findByEmail(String email);
}
