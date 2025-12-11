package com.claimwildcats.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.entity.Claim;


@Repository
public interface ClaimRepository extends JpaRepository<Claim, String>{
    List<Claim> findByItemIdOrderBySubmittedAtDesc(String itemId);
    List<Claim> findByClaimantIdOrderBySubmittedAtDesc(String claimantId);
    boolean existsByItemIdAndClaimantIdAndStatus(String itemId, String claimantId, ClaimStatus status);
    List<Claim> findByStatus(ClaimStatus status);

}
