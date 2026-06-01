package ru.denis.Calculator.Dto;

import java.math.BigDecimal;

public record CalculationItemDto(
        Integer id,
        Short position,
        BigDecimal quantity,
        Integer materialId,
        String materialName,
        BigDecimal materialPrice,
        String materialUnits,
        boolean isConst
) {}
