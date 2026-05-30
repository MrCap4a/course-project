package ru.denis.Calculator.Control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.Calculator.Dto.PermissionDto;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

@RestController
public class UserController {

    private final IUserService userService;
    private final IUserRoleService userRoleService;
    private final PermissionRepository permissionRepository;

    public UserController(IUserService userService,
                          IUserRoleService userRoleService,
                          PermissionRepository permissionRepository) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.permissionRepository = permissionRepository;
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
