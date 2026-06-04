package ru.denis.Calculator.Mediator.Impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.SqlResultDto;
import ru.denis.Calculator.Dto.SqlSchemaDto;
import ru.denis.Calculator.Dto.SqlSchemaDto.ColumnInfo;
<<<<<<< Updated upstream
=======
import ru.denis.Calculator.Dto.SqlSchemaDto.ForeignKey;
>>>>>>> Stashed changes
import ru.denis.Calculator.Dto.SqlSchemaDto.TableInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SqlServiceImpl {

    private static final int MAX_ROWS = 1000;

    private final JdbcTemplate jdbcTemplate;

    public SqlServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlResultDto executeQuery(String query) {
        String trimmed = query.strip();
        if (!trimmed.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("Разрешены только SELECT-запросы");
        }

        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(trimmed);

        if (rawRows.isEmpty()) {
            return new SqlResultDto(List.of(), List.of(), 0);
        }

        List<String> columns = new ArrayList<>(rawRows.get(0).keySet());
        List<Map<String, Object>> rows = rawRows.stream()
                .limit(MAX_ROWS)
                .map(row -> {
                    Map<String, Object> ordered = new LinkedHashMap<>();
                    for (String col : columns) {
                        Object val = row.get(col);
                        ordered.put(col, val != null ? val.toString() : null);
                    }
                    return (Map<String, Object>) ordered;
                })
                .toList();

        return new SqlResultDto(columns, rows, rows.size());
    }

    public SqlSchemaDto getSchema() {
<<<<<<< Updated upstream
=======
        List<TableInfo> tables = loadTables();
        List<ForeignKey> foreignKeys = loadForeignKeys();
        return new SqlSchemaDto(tables, foreignKeys);
    }

    private List<TableInfo> loadTables() {
>>>>>>> Stashed changes
        String sql = """
                SELECT table_name, column_name, data_type, is_nullable
                FROM information_schema.columns
                WHERE table_schema = 'public'
                ORDER BY table_name, ordinal_position
                """;

        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sql);

        Map<String, List<ColumnInfo>> tableMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rawRows) {
            String table = (String) row.get("table_name");
            String column = (String) row.get("column_name");
            String type = (String) row.get("data_type");
            boolean nullable = "YES".equals(row.get("is_nullable"));
            tableMap.computeIfAbsent(table, k -> new ArrayList<>())
                    .add(new ColumnInfo(column, type, nullable));
        }

<<<<<<< Updated upstream
        List<TableInfo> tables = tableMap.entrySet().stream()
                .map(e -> new TableInfo(e.getKey(), e.getValue()))
                .toList();

        return new SqlSchemaDto(tables);
=======
        return tableMap.entrySet().stream()
                .map(e -> new TableInfo(e.getKey(), e.getValue()))
                .toList();
    }

    private List<ForeignKey> loadForeignKeys() {
        String sql = """
                SELECT
                    kcu.table_name   AS from_table,
                    kcu.column_name  AS from_column,
                    ccu.table_name   AS to_table,
                    ccu.column_name  AS to_column
                FROM information_schema.table_constraints AS tc
                JOIN information_schema.key_column_usage AS kcu
                    ON tc.constraint_name = kcu.constraint_name
                    AND tc.table_schema   = kcu.table_schema
                JOIN information_schema.constraint_column_usage AS ccu
                    ON ccu.constraint_name = tc.constraint_name
                    AND ccu.table_schema   = tc.table_schema
                WHERE tc.constraint_type = 'FOREIGN KEY'
                  AND tc.table_schema    = 'public'
                """;

        return jdbcTemplate.queryForList(sql).stream()
                .map(row -> new ForeignKey(
                        (String) row.get("from_table"),
                        (String) row.get("from_column"),
                        (String) row.get("to_table"),
                        (String) row.get("to_column")
                ))
                .toList();
>>>>>>> Stashed changes
    }
}
