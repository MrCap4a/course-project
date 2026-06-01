package ru.denis.calculatordesktop.api.dto;

import java.math.BigDecimal;

public record MaterialDto(
        Integer id,
        String name,
        BigDecimal price,
        String units,
        Integer groupId,
        String groupName
) {
    @Override
    public String toString() { return name != null ? name : ""; }
}
