package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceProxyTest {

    @Mock private IUserService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private UserServiceProxy proxy;

    private UserDto dto() {
        return new UserDto(1, "alice", "Alice", "Smith", false, 1, "Admin");
    }

    @Test
    void getAllUsers_requiresViewPermission() {
        when(delegate.getAllUsers()).thenReturn(List.of());
        proxy.getAllUsers();
        verify(checker).require("users.view");
        verify(delegate).getAllUsers();
    }

    @Test
    void getAllUsers_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("users.view");
        assertThatThrownBy(() -> proxy.getAllUsers()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserById_requiresViewPermission() {
        when(delegate.getUserById(1)).thenReturn(dto());
        proxy.getUserById(1);
        verify(checker).require("users.view");
        verify(delegate).getUserById(1);
    }

    @Test
    void createUser_requiresCreatePermission() {
        RegisterRequest req = new RegisterRequest("alice", "pass", "Alice", "Smith", 1);
        when(delegate.createUser(req)).thenReturn(dto());
        proxy.createUser(req);
        verify(checker).require("users.create");
        verify(delegate).createUser(req);
    }

    @Test
    void editUser_requiresEditPermission() {
        RegisterRequest req = new RegisterRequest("alice2", "pass2", "Alice", "Smith", 1);
        when(delegate.editUser(1, req)).thenReturn(dto());
        proxy.editUser(1, req);
        verify(checker).require("users.edit");
        verify(delegate).editUser(1, req);
    }

    @Test
    void deleteUser_requiresDeletePermission() {
        proxy.deleteUser(1);
        verify(checker).require("users.delete");
        verify(delegate).deleteUser(1);
    }
}
