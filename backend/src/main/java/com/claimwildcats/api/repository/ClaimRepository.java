package com.claimwildcats.api.repository;

import org.springframework.stereotype.Repository;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.entity.Claim;
import java.util.List;


@Repository
public interface ClaimRepository extends org.springframework.data.jpa.repository.JpaRepository<Claim, String>{
    List<Claim> findByItemOrderbySubmittedAtDesc(List<Claim> findByItemId(String itemId);
    List<Claim> findByClaimantIdOrderBySubmittedAtDesc(String claimantId);
    boolean existsByItemIdAndClaimantIdAndStatus(String itemId, String claimantId, ClaimStatus status);
    

}
