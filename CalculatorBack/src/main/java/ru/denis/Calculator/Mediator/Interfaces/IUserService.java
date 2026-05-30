package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;

import java.util.List;

public interface IUserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Integer id);
    UserDto createUser(RegisterRequest request);
    UserDto editUser(Integer id, RegisterRequest request);
    void deleteUser(Integer id);
}
