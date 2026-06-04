package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceProxyTest {

    @Mock private IUserRoleService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private UserRoleServiceProxy proxy;

<<<<<<< Updated upstream
    private UserRoleDto dto() {
        return new UserRoleDto(1, "Admin", List.of());
    }
=======
    private UserRoleDto dto() { return new UserRoleDto(1, "Admin", List.of()); }
>>>>>>> Stashed changes

    @Test
    void getAllRoles_requiresViewPermission() {
        when(delegate.getAllRoles()).thenReturn(List.of());
        proxy.getAllRoles();
        verify(checker).require("roles.view");
<<<<<<< Updated upstream
        verify(delegate).getAllRoles();
=======
>>>>>>> Stashed changes
    }

    @Test
    void getAllRoles_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("roles.view");
        assertThatThrownBy(() -> proxy.getAllRoles()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getRoleById_requiresViewPermission() {
        when(delegate.getRoleById(1)).thenReturn(dto());
        proxy.getRoleById(1);
        verify(checker).require("roles.view");
<<<<<<< Updated upstream
        verify(delegate).getRoleById(1);
=======
>>>>>>> Stashed changes
    }

    @Test
    void createRole_requiresCreatePermission() {
        UserRoleRequest req = new UserRoleRequest("Admin", List.of());
        when(delegate.createRole(req)).thenReturn(dto());
        proxy.createRole(req);
        verify(checker).require("roles.create");
<<<<<<< Updated upstream
        verify(delegate).createRole(req);
=======
>>>>>>> Stashed changes
    }

    @Test
    void editRole_requiresEditPermission() {
        UserRoleRequest req = new UserRoleRequest("SuperAdmin", List.of());
        when(delegate.editRole(1, req)).thenReturn(dto());
        proxy.editRole(1, req);
        verify(checker).require("roles.edit");
<<<<<<< Updated upstream
        verify(delegate).editRole(1, req);
=======
>>>>>>> Stashed changes
    }

    @Test
    void deleteRole_requiresDeletePermission() {
        proxy.deleteRole(1);
        verify(checker).require("roles.delete");
        verify(delegate).deleteRole(1);
    }

    @Test
    void deleteRole_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("roles.delete");
        assertThatThrownBy(() -> proxy.deleteRole(1)).isInstanceOf(AccessDeniedException.class);
    }
}
