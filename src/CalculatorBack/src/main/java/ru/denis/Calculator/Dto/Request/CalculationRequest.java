package ru.denis.Calculator.Dto.Request;

import java.util.List;

public record CalculationRequest(
        String name,
        Integer formulaId,
        List<CalculationItemRequest> items
) {}
