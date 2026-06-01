package ru.denis.calculatordesktop.api.dto;

public record FormulaDto(
        Integer id,
        String name,
        String expression,
        Integer groupId,
        String groupName
) {
    @Override
    public String toString() { return name != null ? name : ""; }
}
