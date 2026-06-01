package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.PermissionDto;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Entity.Permission;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Foundation.UserRoleRepository;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;

import java.util.List;

@Service
public class UserRoleServiceImpl implements IUserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;

    public UserRoleServiceImpl(UserRoleRepository userRoleRepository,
                               PermissionRepository permissionRepository) {
        this.userRoleRepository = userRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<UserRoleDto> getAllRoles() {
        return userRoleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserRoleDto getRoleById(Integer id) {
        return toDto(userRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id)));
    }

    @Override
    public UserRoleDto createRole(UserRoleRequest request) {
        List<Permission> permissions = permissionRepository.findAllById(request.permissionIds());

        UserRole role = new UserRole();
        role.setName(request.name());
        role.setPermissions(permissions);

        return toDto(userRoleRepository.save(role));
    }

    @Override
    public UserRoleDto editRole(Integer id, UserRoleRequest request) {
        UserRole role = userRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if ("Супер-администратор".equals(role.getName())) {
            throw new RuntimeException("Роль супер-администратора нельзя изменить");
        }

        List<Permission> permissions = permissionRepository.findAllById(request.permissionIds());
        role.setName(request.name());
        role.setPermissions(permissions);

        return toDto(userRoleRepository.save(role));
    }

    @Override
    public void deleteRole(Integer id) {
        UserRole role = userRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if ("Супер-администратор".equals(role.getName())) {
            throw new RuntimeException("Роль супер-администратора нельзя удалить");
        }

        userRoleRepository.deleteById(id);
    }

    private UserRoleDto toDto(UserRole role) {
        List<PermissionDto> permissions = role.getPermissions().stream()
                .map(p -> new PermissionDto(p.getId(), p.getName()))
                .toList();
        return new UserRoleDto(role.getId(), role.getName(), permissions);
    }
}
