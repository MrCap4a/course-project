package ru.denis.Calculator.Dto.Request;

import java.math.BigDecimal;

public record CalculationItemRequest(
        Short position,
        BigDecimal quantity,
        Integer materialId
) {}
