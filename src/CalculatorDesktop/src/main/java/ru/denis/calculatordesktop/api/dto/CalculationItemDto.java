package ru.denis.calculatordesktop.api.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationItemDto(
        Integer id,
        Short position,
        BigDecimal quantity,
        Integer materialId,
        String materialName,
        BigDecimal materialPrice,
        String materialUnits,
        @JsonProperty("isConst") boolean isConst
) {}
