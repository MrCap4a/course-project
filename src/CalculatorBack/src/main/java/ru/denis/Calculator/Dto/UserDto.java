package ru.denis.Calculator.Dto;

public record UserDto(
        Integer id,
        String login,
        String name,
        String surname,
        boolean superAdmin,
        Integer roleId,
        String roleName
) {}
