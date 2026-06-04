package ru.denis.Calculator.Config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Entity.Permission;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Foundation.UserRepository;
import ru.denis.Calculator.Foundation.UserRoleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock private PermissionRepository permissionRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MaterialGroupRepository materialGroupRepository;
    @Mock private FormulaGroupRepository formulaGroupRepository;

    private DataInitializer initializer() {
<<<<<<< Updated upstream
        return new DataInitializer(
                permissionRepository, userRoleRepository, userRepository,
                passwordEncoder, materialGroupRepository, formulaGroupRepository
        );
=======
        return new DataInitializer(permissionRepository, userRoleRepository, userRepository,
                passwordEncoder, materialGroupRepository, formulaGroupRepository);
>>>>>>> Stashed changes
    }

    // ── seedPermissions ───────────────────────────────────────────────────────

    @Test
    void run_noExistingPermissions_savesAllRequired() throws Exception {
        when(permissionRepository.findAll()).thenReturn(List.of());
<<<<<<< Updated upstream
        stubSuperAdminRole();
=======
        stubSuperAdminRole(List.of());
>>>>>>> Stashed changes
        stubAdminExists();
        stubDefaultGroups();

        initializer().run(null);

<<<<<<< Updated upstream
        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, atLeastOnce()).save(captor.capture());
        List<String> savedNames = captor.getAllValues().stream()
                .map(Permission::getName).toList();
        assertThat(savedNames).contains(
                "materials.view", "materials.create", "materials.edit", "materials.delete",
                "formulas.view", "formulas.create", "formulas.edit", "formulas.delete",
                "calculations.view", "calculations.create", "calculations.edit", "calculations.delete",
                "roles.view", "roles.create", "roles.edit", "roles.delete",
                "users.view", "users.create", "users.edit", "users.delete",
                "sql.execute"
        );
=======
        ArgumentCaptor<Permission> cap = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, atLeastOnce()).save(cap.capture());
        List<String> names = cap.getAllValues().stream().map(Permission::getName).toList();
        assertThat(names).contains("materials.view", "formulas.create",
                "calculations.delete", "roles.edit", "users.view", "sql.execute");
>>>>>>> Stashed changes
    }

    @Test
    void run_allPermissionsExist_savesNone() throws Exception {
<<<<<<< Updated upstream
        List<Permission> all = buildAllPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRoleWithPermissions(all);
=======
        List<Permission> all = allPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRole(all);
>>>>>>> Stashed changes
        stubAdminExists();
        stubDefaultGroups();

        initializer().run(null);

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void run_somePermissionsMissing_savesOnlyMissing() throws Exception {
<<<<<<< Updated upstream
        Permission existing = permission("materials.view");
        when(permissionRepository.findAll()).thenReturn(List.of(existing));
        stubSuperAdminRole();
=======
        Permission existing = perm("materials.view");
        when(permissionRepository.findAll()).thenReturn(List.of(existing));
        stubSuperAdminRole(List.of(existing));
>>>>>>> Stashed changes
        stubAdminExists();
        stubDefaultGroups();

        initializer().run(null);

<<<<<<< Updated upstream
        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, atLeastOnce()).save(captor.capture());
        List<String> savedNames = captor.getAllValues().stream()
                .map(Permission::getName).toList();
        assertThat(savedNames).doesNotContain("materials.view");
        assertThat(savedNames).contains("materials.create");
=======
        ArgumentCaptor<Permission> cap = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, atLeastOnce()).save(cap.capture());
        List<String> names = cap.getAllValues().stream().map(Permission::getName).toList();
        assertThat(names).doesNotContain("materials.view");
        assertThat(names).contains("sql.execute");
>>>>>>> Stashed changes
    }

    // ── seedSuperAdminRole ────────────────────────────────────────────────────

    @Test
<<<<<<< Updated upstream
    void run_noSuperAdminRole_createsNewRole() throws Exception {
        List<Permission> all = buildAllPermissions();
=======
    void run_noSuperAdminRole_createsNewRoleWithAllPermissions() throws Exception {
        List<Permission> all = allPermissions();
>>>>>>> Stashed changes
        when(permissionRepository.findAll()).thenReturn(all);
        when(userRoleRepository.findAll()).thenReturn(List.of());
        when(userRoleRepository.save(any())).thenReturn(superAdminRole(all));
        stubAdminExists();
        stubDefaultGroups();

        initializer().run(null);

<<<<<<< Updated upstream
        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Супер-администратор");
        assertThat(captor.getValue().getPermissions()).hasSize(all.size());
    }

    @Test
    void run_existingSuperAdminRole_updatesPermissions() throws Exception {
        List<Permission> all = buildAllPermissions();
=======
        ArgumentCaptor<UserRole> cap = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("Супер-администратор");
        assertThat(cap.getValue().getPermissions()).hasSize(all.size());
    }

    @Test
    void run_existingSuperAdminRole_syncesAllPermissions() throws Exception {
        List<Permission> all = allPermissions();
>>>>>>> Stashed changes
        when(permissionRepository.findAll()).thenReturn(all);
        UserRole existing = superAdminRole(List.of());
        when(userRoleRepository.findAll()).thenReturn(List.of(existing));
        when(userRoleRepository.save(any())).thenReturn(existing);
        stubAdminExists();
        stubDefaultGroups();

        initializer().run(null);

<<<<<<< Updated upstream
        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getPermissions()).hasSize(all.size());
=======
        ArgumentCaptor<UserRole> cap = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(cap.capture());
        assertThat(cap.getValue().getPermissions()).hasSize(all.size());
>>>>>>> Stashed changes
    }

    // ── seedAdminUser ─────────────────────────────────────────────────────────

    @Test
    void run_noAdminUser_createsAdminWithEncodedPassword() throws Exception {
<<<<<<< Updated upstream
        List<Permission> all = buildAllPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRoleWithPermissions(all);
=======
        List<Permission> all = allPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRole(all);
>>>>>>> Stashed changes
        when(userRepository.findByLoginWithPermissions("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin")).thenReturn("encoded_admin");
        stubDefaultGroups();
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        initializer().run(null);

<<<<<<< Updated upstream
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getLogin()).isEqualTo("admin");
        assertThat(saved.getPassword()).isEqualTo("encoded_admin");
        assertThat(saved.isSuperAdmin()).isTrue();
=======
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        assertThat(cap.getValue().getLogin()).isEqualTo("admin");
        assertThat(cap.getValue().getPassword()).isEqualTo("encoded_admin");
        assertThat(cap.getValue().isSuperAdmin()).isTrue();
>>>>>>> Stashed changes
    }

    @Test
    void run_adminUserAlreadyExists_doesNotRecreate() throws Exception {
<<<<<<< Updated upstream
        List<Permission> all = buildAllPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRoleWithPermissions(all);
        User existing = new User();
        when(userRepository.findByLoginWithPermissions("admin")).thenReturn(Optional.of(existing));
=======
        List<Permission> all = allPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRole(all);
        when(userRepository.findByLoginWithPermissions("admin"))
                .thenReturn(Optional.of(new User()));
>>>>>>> Stashed changes
        stubDefaultGroups();

        initializer().run(null);

        verify(userRepository, never()).save(any());
    }

    // ── seedDefaultGroups ─────────────────────────────────────────────────────

    @Test
    void run_noDefaultGroups_createsBoth() throws Exception {
<<<<<<< Updated upstream
        List<Permission> all = buildAllPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRoleWithPermissions(all);
=======
        List<Permission> all = allPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRole(all);
>>>>>>> Stashed changes
        stubAdminExists();
        when(materialGroupRepository.findByNameIgnoreCase("Без группы")).thenReturn(Optional.empty());
        when(formulaGroupRepository.findByNameIgnoreCase("Без группы")).thenReturn(Optional.empty());
        when(materialGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formulaGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        initializer().run(null);

        verify(materialGroupRepository).save(any(MaterialGroup.class));
        verify(formulaGroupRepository).save(any(FormulaGroup.class));
    }

    @Test
    void run_defaultGroupsExist_doesNotRecreate() throws Exception {
<<<<<<< Updated upstream
        List<Permission> all = buildAllPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRoleWithPermissions(all);
=======
        List<Permission> all = allPermissions();
        when(permissionRepository.findAll()).thenReturn(all);
        stubSuperAdminRole(all);
>>>>>>> Stashed changes
        stubAdminExists();
        when(materialGroupRepository.findByNameIgnoreCase("Без группы"))
                .thenReturn(Optional.of(new MaterialGroup()));
        when(formulaGroupRepository.findByNameIgnoreCase("Без группы"))
                .thenReturn(Optional.of(new FormulaGroup()));

        initializer().run(null);

        verify(materialGroupRepository, never()).save(any());
        verify(formulaGroupRepository, never()).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

<<<<<<< Updated upstream
    private void stubSuperAdminRole() {
        when(userRoleRepository.findAll()).thenReturn(List.of());
        UserRole role = superAdminRole(List.of());
        when(userRoleRepository.save(any())).thenReturn(role);
    }

    private void stubSuperAdminRoleWithPermissions(List<Permission> perms) {
=======
    private void stubSuperAdminRole(List<Permission> perms) {
>>>>>>> Stashed changes
        UserRole role = superAdminRole(perms);
        when(userRoleRepository.findAll()).thenReturn(List.of(role));
        when(userRoleRepository.save(any())).thenReturn(role);
    }

    private void stubAdminExists() {
        when(userRepository.findByLoginWithPermissions("admin"))
                .thenReturn(Optional.of(new User()));
    }

    private void stubDefaultGroups() {
        when(materialGroupRepository.findByNameIgnoreCase("Без группы"))
                .thenReturn(Optional.of(new MaterialGroup()));
        when(formulaGroupRepository.findByNameIgnoreCase("Без группы"))
                .thenReturn(Optional.of(new FormulaGroup()));
    }

    private UserRole superAdminRole(List<Permission> perms) {
        UserRole r = new UserRole();
        r.setName("Супер-администратор");
        r.setPermissions(perms);
        return r;
    }

<<<<<<< Updated upstream
    private Permission permission(String name) {
=======
    private Permission perm(String name) {
>>>>>>> Stashed changes
        Permission p = new Permission();
        p.setName(name);
        return p;
    }

<<<<<<< Updated upstream
    private List<Permission> buildAllPermissions() {
        return List.of(
                permission("materials.view"), permission("materials.create"),
                permission("materials.edit"), permission("materials.delete"),
                permission("formulas.view"), permission("formulas.create"),
                permission("formulas.edit"), permission("formulas.delete"),
                permission("calculations.view"), permission("calculations.create"),
                permission("calculations.edit"), permission("calculations.delete"),
                permission("roles.view"), permission("roles.create"),
                permission("roles.edit"), permission("roles.delete"),
                permission("users.view"), permission("users.create"),
                permission("users.edit"), permission("users.delete"),
                permission("sql.execute")
=======
    private List<Permission> allPermissions() {
        return List.of(
                perm("materials.view"), perm("materials.create"), perm("materials.edit"), perm("materials.delete"),
                perm("formulas.view"), perm("formulas.create"), perm("formulas.edit"), perm("formulas.delete"),
                perm("calculations.view"), perm("calculations.create"), perm("calculations.edit"), perm("calculations.delete"),
                perm("roles.view"), perm("roles.create"), perm("roles.edit"), perm("roles.delete"),
                perm("users.view"), perm("users.create"), perm("users.edit"), perm("users.delete"),
                perm("sql.execute")
>>>>>>> Stashed changes
        );
    }
}
