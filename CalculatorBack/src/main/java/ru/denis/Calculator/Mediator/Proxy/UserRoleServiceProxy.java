package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;

import java.util.List;

@Service
public class UserRoleServiceProxy implements IUserRoleService {

    private final IUserRoleService delegate;
    private final PermissionChecker checker;

    public UserRoleServiceProxy(
            @Qualifier("userRoleServiceImpl") IUserRoleService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    // Просмотр ролей не требует отдельного права — роли видны всем аутентифицированным
    @Override
    public List<UserRoleDto> getAllRoles() {
        return delegate.getAllRoles();
    }

    @Override
    public UserRoleDto getRoleById(Integer id) {
        return delegate.getRoleById(id);
    }

    @Override
    public UserRoleDto createRole(UserRoleRequest request) {
        checker.require("roles.create");
        return delegate.createRole(request);
    }

    @Override
    public UserRoleDto editRole(Integer id, UserRoleRequest request) {
        checker.require("roles.edit");
        return delegate.editRole(id, request);
    }

    @Override
    public void deleteRole(Integer id) {
        checker.require("roles.delete");
        delegate.deleteRole(id);
    }
}
