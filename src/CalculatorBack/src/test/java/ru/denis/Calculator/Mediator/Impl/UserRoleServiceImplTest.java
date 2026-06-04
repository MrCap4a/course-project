package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Entity.Permission;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Foundation.UserRoleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PermissionRepository permissionRepository;
    @InjectMocks private UserRoleServiceImpl service;

    @Test
    void getAllRoles_returnsMappedList() {
        when(userRoleRepository.findAll()).thenReturn(List.of(role(1, "Admin", List.of())));
        List<UserRoleDto> result = service.getAllRoles();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Admin");
    }

    @Test
    void getAllRoles_withPermissions_mapsThem() {
        when(userRoleRepository.findAll())
                .thenReturn(List.of(role(1, "Viewer", List.of(permission(1, "materials.view")))));
        List<UserRoleDto> result = service.getAllRoles();
        assertThat(result.get(0).permissions().get(0).name()).isEqualTo("materials.view");
    }

    @Test
    void getRoleById_found_returnsDto() {
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role(1, "Admin", List.of())));
        UserRoleDto dto = service.getRoleById(1);
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("Admin");
    }

    @Test
    void getRoleById_notFound_throws() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRoleById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: 99");
    }

    @Test
    void createRole_savesAndReturnsDto() {
        Permission perm = permission(2, "sql.execute");
        when(permissionRepository.findAllById(List.of(2))).thenReturn(List.of(perm));
        when(userRoleRepository.save(any())).thenReturn(role(5, "SqlUser", List.of(perm)));
        UserRoleDto dto = service.createRole(new UserRoleRequest("SqlUser", List.of(2)));
        assertThat(dto.name()).isEqualTo("SqlUser");
        assertThat(dto.permissions()).hasSize(1);
    }

    @Test
    void editRole_regularRole_updatesAndReturns() {
        UserRole existing = role(1, "Editor", List.of());
        Permission perm = permission(3, "calculations.view");
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(permissionRepository.findAllById(List.of(3))).thenReturn(List.of(perm));
        when(userRoleRepository.save(existing)).thenReturn(role(1, "SuperEditor", List.of(perm)));
        UserRoleDto dto = service.editRole(1, new UserRoleRequest("SuperEditor", List.of(3)));
        assertThat(dto.name()).isEqualTo("SuperEditor");
    }

    @Test
    void editRole_superAdminRole_throws() {
        when(userRoleRepository.findById(1))
                .thenReturn(Optional.of(role(1, "Супер-администратор", List.of())));
        assertThatThrownBy(() -> service.editRole(1, new UserRoleRequest("New", List.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("нельзя изменить");
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void editRole_notFound_throws() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.editRole(99, new UserRoleRequest("X", List.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: 99");
    }

    @Test
    void deleteRole_regularRole_deletesById() {
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role(1, "Editor", List.of())));
        service.deleteRole(1);
        verify(userRoleRepository).deleteById(1);
    }

    @Test
    void deleteRole_superAdminRole_throws() {
        when(userRoleRepository.findById(1))
                .thenReturn(Optional.of(role(1, "Супер-администратор", List.of())));
        assertThatThrownBy(() -> service.deleteRole(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("нельзя удалить");
        verify(userRoleRepository, never()).deleteById(any());
    }

    @Test
    void deleteRole_notFound_throws() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteRole(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: 99");
    }

    private UserRole role(int id, String name, List<Permission> permissions) {
        UserRole r = new UserRole();
        r.setId(id);
        r.setName(name);
        r.setPermissions(permissions);
        return r;
    }

    private Permission permission(int id, String name) {
        Permission p = new Permission();
        p.setId(id);
        p.setName(name);
        return p;
    }
}
