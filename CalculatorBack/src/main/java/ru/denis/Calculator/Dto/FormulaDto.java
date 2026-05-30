package ru.denis.Calculator.Dto;

public record FormulaDto(
        Integer id,
        String name,
        String expression,
        Integer groupId,
        String groupName
) {}
