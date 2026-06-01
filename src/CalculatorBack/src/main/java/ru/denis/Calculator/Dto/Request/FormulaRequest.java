package ru.denis.Calculator.Dto.Request;

public record FormulaRequest(
        String name,
        String expression,
        Integer groupId
) {}
