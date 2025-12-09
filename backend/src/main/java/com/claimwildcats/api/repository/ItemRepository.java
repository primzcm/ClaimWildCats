package com.claimwildcats.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
        
    List<Item> findByReporterId(String reporterId);
    //List<Item> findByStatus(ItemStatus status);
   // List<Item> findByTitleContainingIgnoreCase(String keyword);

   // let use this for filtering + pagination instead

   @Query("SELECT i FROM Item i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:zone IS NULL OR i.campusZone = :zone) AND " +
           "(:query IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Item> search(
        @Param("status") ItemStatus status, 
        @Param("zone") CampusZone zone, 
        @Param("query") String query, 
        Pageable pageable
    );
}