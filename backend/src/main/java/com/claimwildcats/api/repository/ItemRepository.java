package com.claimwildcats.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
        
    List<Item> findByReporterId(String reporterId);
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByTitleContainingIgnoreCase(String keyword);
}