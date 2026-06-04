package ru.denis.calculatordesktop.api.dto;

import java.util.List;
import java.util.Map;

public record SqlResultDto(
        List<String> columns,
        List<Map<String, Object>> rows,
        int rowCount
) {}
