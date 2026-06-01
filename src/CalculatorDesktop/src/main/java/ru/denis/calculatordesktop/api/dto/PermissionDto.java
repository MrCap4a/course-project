package ru.denis.calculatordesktop.api.dto;

public record PermissionDto(Integer id, String name) {
    @Override
    public String toString() { return name != null ? name : ""; }
}
