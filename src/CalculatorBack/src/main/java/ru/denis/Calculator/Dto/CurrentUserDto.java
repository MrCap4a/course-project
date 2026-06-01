package ru.denis.Calculator.Dto;

public record CurrentUserDto(
        Integer id,
        String login,
        String name,
        String surname,
        boolean superAdmin,
        UserRoleDto role
) {}
