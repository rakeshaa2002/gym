package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.ProgressEntry;
import com.fitnexus.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {
    List<ProgressEntry> findByUserOrderByEntryDateDescIdDesc(Users user);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ProgressEntry p WHERE p.user.id = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT p.user.id) FROM ProgressEntry p WHERE p.user.id IN :userIds")
    long countDistinctUserByUserIdIn(@org.springframework.data.repository.query.Param("userIds") java.util.List<Long> userIds);
}
