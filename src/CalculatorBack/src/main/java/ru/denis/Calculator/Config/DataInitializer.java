package ru.denis.Calculator.Config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final List<String> REQUIRED_PERMISSIONS = List.of(
            "materials.view", "materials.create", "materials.edit", "materials.delete",
            "formulas.view",  "formulas.create",  "formulas.edit",  "formulas.delete",
            "calculations.view", "calculations.create", "calculations.edit", "calculations.delete",
            "roles.view",  "roles.create",  "roles.edit",  "roles.delete",
            "users.view",  "users.create",  "users.edit",  "users.delete",
            "sql.execute"
    );

    private static final String SUPER_ADMIN_ROLE  = "Супер-администратор";
    private static final String ADMIN_LOGIN        = "admin";
    private static final String ADMIN_PASSWORD     = "admin";
    private static final String ADMIN_NAME         = "Супер";
    private static final String ADMIN_SURNAME      = "Администратор";
    public  static final String DEFAULT_GROUP_NAME = "Без группы";

    private final PermissionRepository    permissionRepository;
    private final UserRoleRepository      userRoleRepository;
    private final UserRepository          userRepository;
    private final PasswordEncoder         passwordEncoder;
    private final MaterialGroupRepository materialGroupRepository;
    private final FormulaGroupRepository  formulaGroupRepository;

    public DataInitializer(PermissionRepository permissionRepository,
                           UserRoleRepository userRoleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           MaterialGroupRepository materialGroupRepository,
                           FormulaGroupRepository formulaGroupRepository) {
        this.permissionRepository    = permissionRepository;
        this.userRoleRepository      = userRoleRepository;
        this.userRepository          = userRepository;
        this.passwordEncoder         = passwordEncoder;
        this.materialGroupRepository = materialGroupRepository;
        this.formulaGroupRepository  = formulaGroupRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedPermissions();
        UserRole superAdminRole = seedSuperAdminRole();
        seedAdminUser(superAdminRole);
        seedDefaultGroups();
    }

    private void seedDefaultGroups() {
        if (materialGroupRepository.findByNameIgnoreCase(DEFAULT_GROUP_NAME).isEmpty()) {
            MaterialGroup mg = new MaterialGroup();
            mg.setName(DEFAULT_GROUP_NAME);
            materialGroupRepository.save(mg);
        }
        if (formulaGroupRepository.findByNameIgnoreCase(DEFAULT_GROUP_NAME).isEmpty()) {
            FormulaGroup fg = new FormulaGroup();
            fg.setName(DEFAULT_GROUP_NAME);
            formulaGroupRepository.save(fg);
        }
    }

    private void seedPermissions() {
        Set<String> existing = permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        for (String name : REQUIRED_PERMISSIONS) {
            if (!existing.contains(name)) {
                Permission p = new Permission();
                p.setName(name);
                permissionRepository.save(p);
            }
        }
    }

    private UserRole seedSuperAdminRole() {
        List<Permission> all = permissionRepository.findAll();
        return userRoleRepository.findAll().stream()
                .filter(r -> SUPER_ADMIN_ROLE.equals(r.getName()))
                .findFirst()
                .map(role -> {
                    role.setPermissions(all);
                    return userRoleRepository.save(role);
                })
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName(SUPER_ADMIN_ROLE);
                    role.setPermissions(all);
                    return userRoleRepository.save(role);
                });
    }

    private void seedAdminUser(UserRole superAdminRole) {
        boolean exists = userRepository.findByLoginWithPermissions(ADMIN_LOGIN).isPresent();
        if (exists) {
            return;
        }

        User admin = new User();
        admin.setLogin(ADMIN_LOGIN);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setName(ADMIN_NAME);
        admin.setSurname(ADMIN_SURNAME);
        admin.setSuperAdmin(true);
        admin.setRole(superAdminRole);
        userRepository.save(admin);
    }
}
