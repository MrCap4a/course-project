package ru.denis.calculatordesktop.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.denis.calculatordesktop.api.dto.CurrentUserDto;
import ru.denis.calculatordesktop.api.dto.PermissionDto;
import ru.denis.calculatordesktop.api.dto.UserRoleDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionManagerTest {

    private final SessionManager session = SessionManager.getInstance();

    @BeforeEach
    void clearSession() {
        session.clear();
    }

    // ── hasPermission ─────────────────────────────────────────────────────────

    @Test
    void hasPermission_superAdmin_alwaysTrue() {
        session.setCurrentUser(superAdmin());
        assertThat(session.hasPermission("sql.execute")).isTrue();
        assertThat(session.hasPermission("anything")).isTrue();
    }

    @Test
    void hasPermission_userWithMatchingPermission_returnsTrue() {
        session.setCurrentUser(userWithPermissions("materials.view", "sql.execute"));
        assertThat(session.hasPermission("sql.execute")).isTrue();
    }

    @Test
    void hasPermission_userWithoutPermission_returnsFalse() {
        session.setCurrentUser(userWithPermissions("materials.view"));
        assertThat(session.hasPermission("sql.execute")).isFalse();
    }

    @Test
    void hasPermission_userWithNoRole_returnsFalse() {
        session.setCurrentUser(userWithNoRole());
        assertThat(session.hasPermission("sql.execute")).isFalse();
    }

    @Test
    void hasPermission_noUser_returnsFalse() {
        assertThat(session.hasPermission("sql.execute")).isFalse();
    }

    @Test
    void hasPermission_userWithEmptyPermissions_returnsFalse() {
        session.setCurrentUser(userWithPermissions());
        assertThat(session.hasPermission("sql.execute")).isFalse();
    }

    // ── isSuperAdmin ──────────────────────────────────────────────────────────

    @Test
    void isSuperAdmin_superAdmin_returnsTrue() {
        session.setCurrentUser(superAdmin());
        assertThat(session.isSuperAdmin()).isTrue();
    }

    @Test
    void isSuperAdmin_regularUser_returnsFalse() {
        session.setCurrentUser(userWithPermissions("roles.view"));
        assertThat(session.isSuperAdmin()).isFalse();
    }

    @Test
    void isSuperAdmin_noUser_returnsFalse() {
        assertThat(session.isSuperAdmin()).isFalse();
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    void clear_resetsTokenAndUser() {
        session.setToken("jwt-token");
        session.setCurrentUser(superAdmin());
        session.clear();
        assertThat(session.getToken()).isNull();
        assertThat(session.getCurrentUser()).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CurrentUserDto superAdmin() {
        return new CurrentUserDto(1, "admin", "Super", "Admin", true, null);
    }

    private CurrentUserDto userWithNoRole() {
        return new CurrentUserDto(2, "user", "John", "Doe", false, null);
    }

    private CurrentUserDto userWithPermissions(String... perms) {
        List<PermissionDto> permList = java.util.Arrays.stream(perms)
                .map(name -> new PermissionDto(0, name))
                .toList();
        UserRoleDto role = new UserRoleDto(1, "Role", permList);
        return new CurrentUserDto(2, "user", "John", "Doe", false, role);
    }
}
