package ru.denis.Calculator.Dto;

import java.math.BigDecimal;

public record MaterialDto(
        Integer id,
        String name,
        BigDecimal price,
        String units,
        Integer groupId,
        String groupName
) {}
