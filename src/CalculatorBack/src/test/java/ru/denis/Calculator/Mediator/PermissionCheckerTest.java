package ru.denis.Calculator.Mediator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.denis.Calculator.Entity.Permission;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionCheckerTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private PermissionChecker checker;

    @AfterEach
<<<<<<< Updated upstream
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── super admin ───────────────────────────────────────────────────────────

    @Test
    void require_superAdmin_passesWithoutCheckingPermissions() {
        User admin = user(true, null);
        setupContext("admin");
        when(userRepository.findByLoginWithPermissions("admin")).thenReturn(Optional.of(admin));

        assertThatNoException().isThrownBy(() -> checker.require("anything.you.want"));
    }

    // ── user with permission ──────────────────────────────────────────────────

    @Test
    void require_userWithMatchingPermission_passes() {
        Permission perm = permission("materials.view");
        User user = user(false, role(List.of(perm)));
        setupContext("alice");
        when(userRepository.findByLoginWithPermissions("alice")).thenReturn(Optional.of(user));

=======
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void require_superAdmin_passesWithoutCheckingPermissions() {
        setupContext("admin");
        when(userRepository.findByLoginWithPermissions("admin"))
                .thenReturn(Optional.of(user(true, null)));
        assertThatNoException().isThrownBy(() -> checker.require("anything"));
    }

    @Test
    void require_userWithMatchingPermission_passes() {
        setupContext("alice");
        when(userRepository.findByLoginWithPermissions("alice"))
                .thenReturn(Optional.of(user(false, role(List.of(permission("materials.view"))))));
>>>>>>> Stashed changes
        assertThatNoException().isThrownBy(() -> checker.require("materials.view"));
    }

    @Test
<<<<<<< Updated upstream
    void require_userWithMultiplePermissions_passesForEach() {
        Permission p1 = permission("materials.view");
        Permission p2 = permission("formulas.create");
        User user = user(false, role(List.of(p1, p2)));
        setupContext("alice");
        when(userRepository.findByLoginWithPermissions("alice")).thenReturn(Optional.of(user));

        assertThatNoException().isThrownBy(() -> checker.require("formulas.create"));
    }

    // ── missing permission ────────────────────────────────────────────────────

    @Test
    void require_userMissingPermission_throwsAccessDenied() {
        Permission perm = permission("materials.view");
        User user = user(false, role(List.of(perm)));
        setupContext("bob");
        when(userRepository.findByLoginWithPermissions("bob")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> checker.require("materials.delete"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("materials.delete");
=======
    void require_userWithSqlExecutePermission_passes() {
        setupContext("alice");
        when(userRepository.findByLoginWithPermissions("alice"))
                .thenReturn(Optional.of(user(false, role(List.of(permission("sql.execute"))))));
        assertThatNoException().isThrownBy(() -> checker.require("sql.execute"));
    }

    @Test
    void require_userWithMultiplePermissions_passesForEach() {
        setupContext("alice");
        when(userRepository.findByLoginWithPermissions("alice"))
                .thenReturn(Optional.of(user(false,
                        role(List.of(permission("materials.view"), permission("sql.execute"))))));
        assertThatNoException().isThrownBy(() -> checker.require("sql.execute"));
    }

    @Test
    void require_userMissingPermission_throwsAccessDenied() {
        setupContext("bob");
        when(userRepository.findByLoginWithPermissions("bob"))
                .thenReturn(Optional.of(user(false, role(List.of(permission("materials.view"))))));
        assertThatThrownBy(() -> checker.require("sql.execute"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("sql.execute");
>>>>>>> Stashed changes
    }

    @Test
    void require_userWithEmptyPermissions_throwsAccessDenied() {
<<<<<<< Updated upstream
        User user = user(false, role(List.of()));
        setupContext("bob");
        when(userRepository.findByLoginWithPermissions("bob")).thenReturn(Optional.of(user));

=======
        setupContext("bob");
        when(userRepository.findByLoginWithPermissions("bob"))
                .thenReturn(Optional.of(user(false, role(List.of()))));
>>>>>>> Stashed changes
        assertThatThrownBy(() -> checker.require("calculations.view"))
                .isInstanceOf(AccessDeniedException.class);
    }

<<<<<<< Updated upstream
    // ── no role ───────────────────────────────────────────────────────────────

    @Test
    void require_userWithNoRole_throwsAccessDenied() {
        User user = user(false, null);
        setupContext("norole");
        when(userRepository.findByLoginWithPermissions("norole")).thenReturn(Optional.of(user));

=======
    @Test
    void require_userWithNoRole_throwsAccessDenied() {
        setupContext("norole");
        when(userRepository.findByLoginWithPermissions("norole"))
                .thenReturn(Optional.of(user(false, null)));
>>>>>>> Stashed changes
        assertThatThrownBy(() -> checker.require("materials.view"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no role");
    }

<<<<<<< Updated upstream
    // ── user not found ────────────────────────────────────────────────────────

=======
>>>>>>> Stashed changes
    @Test
    void require_userNotInDatabase_throwsRuntime() {
        setupContext("ghost");
        when(userRepository.findByLoginWithPermissions("ghost")).thenReturn(Optional.empty());
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
        assertThatThrownBy(() -> checker.require("materials.view"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ghost");
    }

<<<<<<< Updated upstream
    // ── helpers ───────────────────────────────────────────────────────────────

=======
>>>>>>> Stashed changes
    private void setupContext(String login) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(login);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private User user(boolean superAdmin, UserRole role) {
        User u = new User();
        u.setSuperAdmin(superAdmin);
        u.setRole(role);
        return u;
    }

    private UserRole role(List<Permission> permissions) {
        UserRole r = new UserRole();
        r.setPermissions(permissions);
        return r;
    }

    private Permission permission(String name) {
        Permission p = new Permission();
        p.setName(name);
        return p;
    }
}
