package ru.denis.Calculator.Dto.Request;

public record LoginRequest(
        String login,
        String password
) {}
