package ru.denis.Calculator.Dto;

public record TokenResponse(
        String token,
        String tokenType,
        String refreshToken
) {
    public static TokenResponse bearer(String token, String refreshToken) {
        return new TokenResponse(token, "Bearer", refreshToken);
    }
}
