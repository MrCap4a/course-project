package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

@Service
public class UserServiceProxy implements IUserService {

    private final IUserService delegate;
    private final PermissionChecker checker;

    public UserServiceProxy(
            @Qualifier("userServiceImpl") IUserService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    // Просмотр и редактирование пользователей не требуют отдельных прав
    @Override
    public List<UserDto> getAllUsers() {
        return delegate.getAllUsers();
    }

    @Override
    public UserDto getUserById(Integer id) {
        return delegate.getUserById(id);
    }

    @Override
    public UserDto createUser(RegisterRequest request) {
        checker.require("users.create");
        return delegate.createUser(request);
    }

    @Override
    public UserDto editUser(Integer id, RegisterRequest request) {
        return delegate.editUser(id, request);
    }

    @Override
    public void deleteUser(Integer id) {
        checker.require("users.delete");
        delegate.deleteUser(id);
    }
}
