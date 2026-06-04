package ru.denis.Calculator.Dto;

import java.util.List;

<<<<<<< Updated upstream
public record SqlSchemaDto(List<TableInfo> tables) {
=======
public record SqlSchemaDto(List<TableInfo> tables, List<ForeignKey> foreignKeys) {
>>>>>>> Stashed changes

    public record TableInfo(String name, List<ColumnInfo> columns) {}

    public record ColumnInfo(String name, String type, boolean nullable) {}
<<<<<<< Updated upstream
=======

    public record ForeignKey(
            String fromTable,
            String fromColumn,
            String toTable,
            String toColumn
    ) {}
>>>>>>> Stashed changes
}
