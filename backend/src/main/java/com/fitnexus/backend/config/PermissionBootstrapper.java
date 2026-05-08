package com.fitnexus.backend.config;

import com.fitnexus.backend.entity.PermissionPage;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.RolePagePermission;
import com.fitnexus.backend.repository.PermissionPageRepository;
import com.fitnexus.backend.repository.RolePagePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PermissionBootstrapper implements CommandLineRunner {

    private final PermissionPageRepository permissionPageRepository;
    private final RolePagePermissionRepository rolePagePermissionRepository;

    private record PageSeed(String pageKey, String label, String routePath, String category, int sortOrder) {}
    private record PermissionSeed(String pageKey, boolean view, boolean create, boolean edit, boolean delete) {}

    @Override
    @Transactional
    public void run(String... args) {
        seedPages();
        seedDefaultPermissions();
    }

    private void seedPages() {
        List<PageSeed> seeds = List.of(
                new PageSeed("dashboard", "Overview", "/", "Core", 1),
                new PageSeed("employees", "Employees", "/employees", "Management", 10),
                new PageSeed("users", "Users", "/users", "Management", 11),
                new PageSeed("headoffice", "Head Office", "/headoffice", "Management", 12),
                new PageSeed("branches", "Branches", "/branches", "Management", 13),
                new PageSeed("departments", "Departments", "/departments", "Management", 14),
                new PageSeed("designations", "Designations", "/designations", "Management", 15),
                new PageSeed("teams", "Teams", "/teams", "Management", 16),
                new PageSeed("workout-filter", "Workout Filter", "/workout-filter", "Workout", 20),
                new PageSeed("workout-topfilter", "Workout Top Filter", "/workout-topfilter", "Workout", 21),
                new PageSeed("upperbody-workout", "Body Workout", "/upperbody-workout", "Workout", 22),
                new PageSeed("create-workout", "Create Workout", "/create-workout", "Workout", 23),
                new PageSeed("workout-summary", "Workout Summary", "/workout-summary", "Workout", 24),
                new PageSeed("workout-type", "Workout Type Master", "/workout-type", "Workout", 25),
                new PageSeed("body-part", "Body Part Master", "/body-part", "Workout", 26),
                new PageSeed("exercise-master", "Exercise Master", "/exercise-master", "Workout", 27),
                new PageSeed("workout-plan", "Workout Plan Master", "/workout-plan", "Workout", 28),
                new PageSeed("workout-detail", "Workout Detail", "/workout-detail", "Workout", 29),
                new PageSeed("trainer-duty-schedule", "Trainer Duty Schedule", "/trainer-duty-schedule", "Schedule", 40),
                new PageSeed("user-workout-schedule", "User Workout Schedule", "/user-workout-schedule", "Schedule", 41),
                new PageSeed("my-schedule", "My Schedule", "/my-schedule", "Schedule", 42),
                new PageSeed("dietplan", "Diet Menu", "/dietplan", "Diet Menu", 30),
                new PageSeed("diet-detail", "Diet Detail", "/diet-detail", "Diet Menu", 31),
                new PageSeed("goals", "Goals", "/goals", "Fitness", 40),
                new PageSeed("schedule", "My Schedule", "/schedule", "Fitness", 41),
                new PageSeed("progress", "Progress", "/progress", "Fitness", 42),
                new PageSeed("profile", "Profile", "/profile", "Account", 50),
                new PageSeed("onboding-step", "Step", "/onboding-step", "Account", 51),
                new PageSeed("role-permissions", "Role Permissions", "/role-permissions", "Management", 5)
        );

        for (PageSeed seed : seeds) {
            PermissionPage page = permissionPageRepository.findByPageKey(seed.pageKey()).orElseGet(() -> {
                PermissionPage newPage = new PermissionPage();
                newPage.setPageKey(seed.pageKey());
                return newPage;
            });
            page.setLabel(seed.label());
            page.setRoutePath(seed.routePath());
            page.setCategory(seed.category());
            page.setSortOrder(seed.sortOrder());
            permissionPageRepository.save(page);
        }
    }

    private void seedDefaultPermissions() {
        Map<Role, List<PermissionSeed>> defaults = Map.of(
                Role.SUPER_ADMIN, List.of(
                        new PermissionSeed("dashboard", true, true, true, true),
                        new PermissionSeed("employees", true, true, true, true),
                        new PermissionSeed("users", true, true, true, true),
                        new PermissionSeed("headoffice", true, true, true, true),
                        new PermissionSeed("branches", true, true, true, true),
                        new PermissionSeed("departments", true, true, true, true),
                        new PermissionSeed("designations", true, true, true, true),
                        new PermissionSeed("teams", true, true, true, true),
                        new PermissionSeed("workout-filter", true, true, true, true),
                        new PermissionSeed("workout-topfilter", true, true, true, true),
                        new PermissionSeed("upperbody-workout", true, true, true, true),
                        new PermissionSeed("create-workout", true, true, true, true),
                        new PermissionSeed("workout-summary", true, true, true, true),
                        new PermissionSeed("workout-type", true, true, true, true),
                        new PermissionSeed("body-part", true, true, true, true),
                        new PermissionSeed("exercise-master", true, true, true, true),
                        new PermissionSeed("workout-plan", true, true, true, true),
                        new PermissionSeed("workout-detail", true, true, true, true),
                        new PermissionSeed("trainer-duty-schedule", true, true, true, true),
                        new PermissionSeed("user-workout-schedule", true, true, true, true),
                        new PermissionSeed("my-schedule", true, false, false, false),
                        new PermissionSeed("dietplan", true, true, true, true),
                        new PermissionSeed("diet-detail", true, true, true, true),
                        new PermissionSeed("goals", true, true, true, true),
                        new PermissionSeed("schedule", true, true, true, true),
                        new PermissionSeed("progress", true, true, true, true),
                        new PermissionSeed("profile", true, true, true, true),
                        new PermissionSeed("onboding-step", true, true, true, true),
                        new PermissionSeed("role-permissions", true, true, true, true)
                ),
                Role.ADMIN, List.of(
                        new PermissionSeed("dashboard", true, false, false, false),
                        new PermissionSeed("employees", true, true, true, true),
                        new PermissionSeed("users", true, true, true, true),
                        new PermissionSeed("headoffice", true, true, true, true),
                        new PermissionSeed("branches", true, true, true, true),
                        new PermissionSeed("departments", true, true, true, true),
                        new PermissionSeed("designations", true, true, true, true),
                        new PermissionSeed("teams", true, true, true, true),
                        new PermissionSeed("schedule", true, false, false, false),
                        new PermissionSeed("profile", true, false, false, false),
                        new PermissionSeed("onboding-step", true, false, false, false),
                        new PermissionSeed("workout-type", true, true, true, true),
                        new PermissionSeed("body-part", true, true, true, true),
                        new PermissionSeed("exercise-master", true, true, true, true),
                        new PermissionSeed("workout-plan", true, true, true, true),
                        new PermissionSeed("workout-detail", true, true, true, true)
                ),
                Role.MANAGER, List.of(
                        new PermissionSeed("dashboard", true, false, false, false),
                        new PermissionSeed("employees", true, true, true, true),
                        new PermissionSeed("users", true, true, true, true),
                        new PermissionSeed("branches", true, true, true, true),
                        new PermissionSeed("departments", true, true, true, true),
                        new PermissionSeed("designations", true, true, true, true),
                        new PermissionSeed("teams", true, true, true, true),
                        new PermissionSeed("schedule", true, false, false, false),
                        new PermissionSeed("profile", true, false, false, false),
                        new PermissionSeed("onboding-step", true, false, false, false),
                        new PermissionSeed("workout-type", true, true, true, true),
                        new PermissionSeed("body-part", true, true, true, true),
                        new PermissionSeed("exercise-master", true, true, true, true),
                        new PermissionSeed("workout-plan", true, true, true, true),
                        new PermissionSeed("workout-detail", true, true, true, true),
                        new PermissionSeed("trainer-duty-schedule", true, true, true, true),
                        new PermissionSeed("user-workout-schedule", true, true, true, true),
                        new PermissionSeed("my-schedule", false, false, false, false)
                ),
                Role.TRAINER, List.of(
                        new PermissionSeed("dashboard", true, false, false, false),
                        new PermissionSeed("users", true, true, true, true),
                        new PermissionSeed("workout-filter", true, false, false, false),
                        new PermissionSeed("workout-topfilter", true, false, false, false),
                        new PermissionSeed("upperbody-workout", true, false, false, false),
                        new PermissionSeed("create-workout", true, true, true, true),
                        new PermissionSeed("workout-summary", true, false, false, false),
                        new PermissionSeed("workout-type", true, true, true, true),
                        new PermissionSeed("body-part", true, true, true, true),
                        new PermissionSeed("exercise-master", true, true, true, true),
                        new PermissionSeed("workout-plan", true, true, true, true),
                        new PermissionSeed("workout-detail", true, true, true, true),
                        new PermissionSeed("trainer-duty-schedule", false, false, false, false),
                        new PermissionSeed("user-workout-schedule", true, true, true, true),
                        new PermissionSeed("my-schedule", false, false, false, false),
                        new PermissionSeed("dietplan", true, false, false, false),
                        new PermissionSeed("diet-detail", true, false, false, false),
                        new PermissionSeed("goals", true, false, false, false),
                        new PermissionSeed("schedule", true, false, false, false),
                        new PermissionSeed("progress", true, false, false, false),
                        new PermissionSeed("profile", true, false, false, false),
                        new PermissionSeed("onboding-step", true, false, false, false)
                ),
                Role.USER, List.of(
                        new PermissionSeed("dashboard", true, false, false, false),
                        new PermissionSeed("workout-detail", true, false, false, false),
                        new PermissionSeed("trainer-duty-schedule", false, false, false, false),
                        new PermissionSeed("user-workout-schedule", false, false, false, false),
                        new PermissionSeed("my-schedule", true, false, false, false),
                        new PermissionSeed("dietplan", true, false, false, false),
                        new PermissionSeed("diet-detail", true, false, false, false),
                        new PermissionSeed("goals", true, false, false, false),
                        new PermissionSeed("schedule", true, false, false, false),
                        new PermissionSeed("progress", true, false, false, false),
                        new PermissionSeed("profile", true, false, false, false),
                        new PermissionSeed("onboding-step", true, false, false, false)
                )
        );

        for (Map.Entry<Role, List<PermissionSeed>> entry : defaults.entrySet()) {
            for (PermissionSeed seed : entry.getValue()) {
                rolePagePermissionRepository.findByRoleAndPageKey(entry.getKey(), seed.pageKey())
                        .orElseGet(() -> {
                            RolePagePermission permission = new RolePagePermission();
                            permission.setRole(entry.getKey());
                            permission.setPageKey(seed.pageKey());
                            permission.setCanView(seed.view());
                            permission.setCanCreate(seed.create());
                            permission.setCanEdit(seed.edit());
                            permission.setCanDelete(seed.delete());
                            return rolePagePermissionRepository.save(permission);
                        });
            }
        }
    }
}
