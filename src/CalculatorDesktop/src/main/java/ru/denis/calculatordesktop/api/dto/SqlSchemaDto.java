package ru.denis.calculatordesktop.api.dto;

import java.util.List;

public record SqlSchemaDto(
        List<TableInfo> tables,
        List<ForeignKey> foreignKeys
) {
    public record TableInfo(String name, List<ColumnInfo> columns) {}
    public record ColumnInfo(String name, String type, boolean nullable) {}
    public record ForeignKey(String fromTable, String fromColumn, String toTable, String toColumn) {}
}
