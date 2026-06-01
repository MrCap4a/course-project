package ru.denis.Calculator.Dto;

import java.util.List;

public record UserRoleDto(
        Integer id,
        String name,
        List<PermissionDto> permissions
) {}
