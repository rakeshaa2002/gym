package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.CurrentUserPermissionsResponse;
import com.fitnexus.backend.dto.PermissionsMatrixResponse;
import com.fitnexus.backend.dto.PermissionMatrixUpdateRequest;
import com.fitnexus.backend.dto.PermissionPageDto;
import com.fitnexus.backend.dto.PagePermissionDto;
import com.fitnexus.backend.dto.RolePermissionsDto;
import com.fitnexus.backend.entity.PermissionPage;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.RolePagePermission;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.PermissionPageRepository;
import com.fitnexus.backend.repository.RolePagePermissionRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionPageRepository permissionPageRepository;
    private final RolePagePermissionRepository rolePagePermissionRepository;
    private final UserRepository userRepository;

    public List<PermissionPageDto> getPages() {
        return permissionPageRepository.findAll().stream()
                .sorted(Comparator.comparing(PermissionPage::getSortOrder).thenComparing(PermissionPage::getLabel))
                .map(this::toPageDto)
                .toList();
    }

    public CurrentUserPermissionsResponse getCurrentUserPermissions(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("Current user not found"));
        Role role = user.getRole();
        return new CurrentUserPermissionsResponse(role, getPermissionsForRole(role));
    }

    public PermissionsMatrixResponse getMatrix() {
        List<PermissionPage> pageEntities = getSortedPages();
        List<PermissionPageDto> pages = pageEntities.stream().map(this::toPageDto).toList();
        List<RolePermissionsDto> roles = new ArrayList<>();

        for (Role role : Role.values()) {
            roles.add(new RolePermissionsDto(role, getPermissionsForRole(role, pageEntities)));
        }

        return new PermissionsMatrixResponse(pages, roles);
    }

    public List<PagePermissionDto> getPermissionsForRole(Role role) {
        return getPermissionsForRole(role, getSortedPages());
    }

    private List<PagePermissionDto> getPermissionsForRole(Role role, List<PermissionPage> pages) {
        Map<String, RolePagePermission> byPage = new HashMap<>();
        rolePagePermissionRepository.findByRole(role).forEach(item -> byPage.put(item.getPageKey(), item));

        return pages.stream()
                .map(page -> {
                    RolePagePermission rp = byPage.get(page.getPageKey());
                    if (rp != null) {
                        return new PagePermissionDto(page.getPageKey(), rp.isCanView(), rp.isCanCreate(), rp.isCanEdit(), rp.isCanDelete());
                    }
                    return new PagePermissionDto(page.getPageKey(), false, false, false, false);
                })
                .toList();
    }

    public void saveMatrix(PermissionMatrixUpdateRequest request) {
        if (request == null || request.getRoles() == null) {
            throw new IllegalArgumentException("Permission matrix is required");
        }

        Map<String, PermissionPage> pageLookup = new HashMap<>();
        permissionPageRepository.findAll().forEach(page -> pageLookup.put(page.getPageKey(), page));

        for (RolePermissionsDto roleDto : request.getRoles()) {
            if (roleDto == null || roleDto.getRole() == null) {
                continue;
            }

            List<PagePermissionDto> permissions = roleDto.getPermissions() == null ? List.of() : roleDto.getPermissions();
            Map<String, PagePermissionDto> submittedByPage = new HashMap<>();
            for (PagePermissionDto permission : permissions) {
                if (permission == null || permission.getPageKey() == null || !pageLookup.containsKey(permission.getPageKey())) {
                    continue;
                }
                submittedByPage.put(permission.getPageKey(), permission);
            }

            if (submittedByPage.isEmpty()) {
                rolePagePermissionRepository.deleteByRole(roleDto.getRole());
                continue;
            }

            rolePagePermissionRepository.deleteByRoleAndPageKeyNotIn(roleDto.getRole(), submittedByPage.keySet());

            Map<String, RolePagePermission> existingByPage = new HashMap<>();
            rolePagePermissionRepository.findByRole(roleDto.getRole())
                    .forEach(permission -> existingByPage.put(permission.getPageKey(), permission));

            List<RolePagePermission> entities = new ArrayList<>();
            for (PagePermissionDto permission : submittedByPage.values()) {
                RolePagePermission entity = existingByPage.getOrDefault(permission.getPageKey(), new RolePagePermission());
                entity.setRole(roleDto.getRole());
                entity.setPageKey(permission.getPageKey());
                entity.setCanView(permission.isCanView());
                entity.setCanCreate(permission.isCanCreate());
                entity.setCanEdit(permission.isCanEdit());
                entity.setCanDelete(permission.isCanDelete());
                entities.add(entity);
            }
            rolePagePermissionRepository.saveAll(entities);
        }
    }

    public boolean hasPermission(Role role, String pageKey, String action) {
        RolePagePermission permission = rolePagePermissionRepository.findByRoleAndPageKey(role, pageKey).orElse(null);
        if (permission == null) {
            return false;
        }
        return switch (action.toLowerCase()) {
            case "create" -> permission.isCanCreate();
            case "edit" -> permission.isCanEdit();
            case "delete" -> permission.isCanDelete();
            default -> permission.isCanView();
        };
    }

    private PermissionPageDto toPageDto(PermissionPage page) {
        return new PermissionPageDto(
                page.getPageKey(),
                page.getLabel(),
                page.getRoutePath(),
                page.getCategory(),
                page.getSortOrder()
        );
    }

    private List<PermissionPage> getSortedPages() {
        return permissionPageRepository.findAll().stream()
                .sorted(Comparator.comparing(PermissionPage::getSortOrder).thenComparing(PermissionPage::getLabel))
                .toList();
    }
}
