package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.PermissionPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionPageRepository extends JpaRepository<PermissionPage, Long> {
    Optional<PermissionPage> findByPageKey(String pageKey);
}
