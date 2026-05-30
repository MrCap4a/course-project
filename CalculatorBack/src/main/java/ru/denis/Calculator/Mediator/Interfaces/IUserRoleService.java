package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;

import java.util.List;

public interface IUserRoleService {
    List<UserRoleDto> getAllRoles();
    UserRoleDto getRoleById(Integer id);
    UserRoleDto createRole(UserRoleRequest request);
    UserRoleDto editRole(Integer id, UserRoleRequest request);
    void deleteRole(Integer id);
}
