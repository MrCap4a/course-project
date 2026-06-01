package ru.denis.calculatordesktop.api.dto;

public record UserDto(
        Integer id,
        String login,
        String name,
        String surname,
        boolean superAdmin,
        Integer roleId,
        String roleName
) {
    @Override
    public String toString() {
        return (name != null ? name : "") + " " + (surname != null ? surname : "") + " (" + login + ")";
    }
}
