package ru.denis.calculatordesktop.api.dto;

import java.util.List;

public record UserRoleDto(
        Integer id,
        String name,
        List<PermissionDto> permissions
) {
    @Override
    public String toString() { return name != null ? name : ""; }
}
