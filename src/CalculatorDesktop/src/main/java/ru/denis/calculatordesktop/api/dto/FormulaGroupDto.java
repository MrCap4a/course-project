package ru.denis.calculatordesktop.api.dto;

public record FormulaGroupDto(Integer id, String name) {
    @Override
    public String toString() { return name != null ? name : ""; }
}
