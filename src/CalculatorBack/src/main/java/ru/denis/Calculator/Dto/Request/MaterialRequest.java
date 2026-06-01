package ru.denis.Calculator.Dto.Request;

import java.math.BigDecimal;

public record MaterialRequest(
        String name,
        BigDecimal price,
        String units,
        Integer groupId
) {}
