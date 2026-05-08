package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.RolePagePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RolePagePermissionRepository extends JpaRepository<RolePagePermission, Long> {
    List<RolePagePermission> findByRole(Role role);

    void deleteByRole(Role role);

    void deleteByRoleAndPageKeyNotIn(Role role, Collection<String> pageKeys);

    Optional<RolePagePermission> findByRoleAndPageKey(Role role, String pageKey);
}
