package ru.denis.calculatordesktop.api.dto;

public record CurrentUserDto(
        Integer id,
        String login,
        String name,
        String surname,
        boolean superAdmin,
        UserRoleDto role
) {
    public String displayName() {
        String n = (name != null ? name : "") + " " + (surname != null ? surname : "");
        return n.isBlank() ? login : n.trim();
    }
}
