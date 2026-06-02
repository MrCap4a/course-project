package ru.denis.Calculator.Control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.denis.Calculator.Dto.CurrentUserDto;
import ru.denis.Calculator.Dto.PermissionDto;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Foundation.UserRepository;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

@RestController
public class UserController {

    private final IUserService userService;
    private final IUserRoleService userRoleService;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public UserController(IUserService userService,
                          IUserRoleService userRoleService,
                          PermissionRepository permissionRepository,
                          UserRepository userRepository) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> result = permissionRepository.findAll().stream()
                .map(p -> new PermissionDto(p.getId(), p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------------------
    // Current user (no permission check — needed for auth context)
    // -------------------------------------------------------------------------

    @GetMapping("/users/me")
    public ResponseEntity<CurrentUserDto> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByLoginWithPermissions(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRoleDto roleDto = null;
        if (user.getRole() != null) {
            List<PermissionDto> perms = user.getRole().getPermissions().stream()
                    .map(p -> new PermissionDto(p.getId(), p.getName()))
                    .toList();
            roleDto = new UserRoleDto(user.getRole().getId(), user.getRole().getName(), perms);
        }

        return ResponseEntity.ok(new CurrentUserDto(
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getSurname(),
                user.isSuperAdmin(),
                roleDto
        ));
    }

    // -------------------------------------------------------------------------
    // Users
    // -------------------------------------------------------------------------

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> editUser(@PathVariable Integer id,
                                            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.editUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Roles
    // -------------------------------------------------------------------------

    @GetMapping("/roles")
    public ResponseEntity<List<UserRoleDto>> getAllRoles() {
        return ResponseEntity.ok(userRoleService.getAllRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<UserRoleDto> createRole(@RequestBody UserRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<UserRoleDto> editRole(@PathVariable Integer id,
                                                @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(userRoleService.editRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        userRoleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
