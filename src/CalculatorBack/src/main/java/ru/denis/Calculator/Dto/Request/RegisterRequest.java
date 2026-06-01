package ru.denis.Calculator.Dto.Request;

public record RegisterRequest(
        String login,
        String password,
        String name,
        String surname,
        Integer roleId
) {}
