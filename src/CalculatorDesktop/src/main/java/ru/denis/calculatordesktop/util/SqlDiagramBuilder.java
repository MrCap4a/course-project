package ru.denis.calculatordesktop.util;

import ru.denis.calculatordesktop.api.dto.SqlSchemaDto;

import java.util.HashSet;
import java.util.Set;

public final class SqlDiagramBuilder {

    private SqlDiagramBuilder() {}

    public static String buildErDiagram(SqlSchemaDto schema) {
        StringBuilder sb = new StringBuilder("erDiagram\n");

        for (SqlSchemaDto.TableInfo table : schema.tables()) {
            sb.append("  ").append(table.name()).append(" {\n");
            for (SqlSchemaDto.ColumnInfo col : table.columns()) {
                String safeType = col.type().replace(" ", "_");
                String nullable = col.nullable() ? "nullable" : "required";
                sb.append("    ").append(safeType).append(" ")
                  .append(col.name()).append(" \"").append(nullable).append("\"\n");
            }
            sb.append("  }\n");
        }

        Set<String> seen = new HashSet<>();
        for (SqlSchemaDto.ForeignKey fk : schema.foreignKeys()) {
            String key = fk.fromTable() + "-" + fk.toTable();
            if (!seen.contains(key)) {
                seen.add(key);
                sb.append("  ").append(fk.fromTable())
                  .append(" }o--|| ").append(fk.toTable())
                  .append(" : \"").append(fk.fromColumn()).append("\"\n");
            }
        }

        return sb.toString();
    }

    public static String escapeForHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
