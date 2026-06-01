package ru.denis.Calculator.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculationDto(
        Integer id,
        String name,
        Integer formulaId,
        String formulaName,
        String formulaExpression,
        Integer formulaGroupId,
        String formulaGroupName,
        List<CalculationItemDto> items,
        BigDecimal result
) {}
