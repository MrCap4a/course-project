package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.denis.Calculator.Dto.SqlResultDto;
import ru.denis.Calculator.Dto.SqlSchemaDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlServiceImplTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private SqlServiceImpl service;

    // ── executeQuery ──────────────────────────────────────────────────────────

    @Test
    void executeQuery_validSelect_returnsColumnsAndRows() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "Alice");
        when(jdbcTemplate.queryForList("SELECT * FROM \"user\"")).thenReturn(List.of(row));

        SqlResultDto result = service.executeQuery("SELECT * FROM \"user\"");

        assertThat(result.columns()).containsExactly("id", "name");
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).get("id")).isEqualTo("1");
        assertThat(result.rows().get(0).get("name")).isEqualTo("Alice");
        assertThat(result.rowCount()).isEqualTo(1);
    }

    @Test
    void executeQuery_leadingWhitespace_isAccepted() {
        when(jdbcTemplate.queryForList("SELECT 1")).thenReturn(List.of());
        assertThat(service.executeQuery("  SELECT 1").rowCount()).isEqualTo(0);
    }

    @Test
    void executeQuery_caseInsensitive_isAccepted() {
        when(jdbcTemplate.queryForList("select * from material")).thenReturn(List.of());
        assertThat(service.executeQuery("select * from material").rowCount()).isEqualTo(0);
    }

    @Test
    void executeQuery_insertQuery_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.executeQuery("INSERT INTO material VALUES (1)"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SELECT");
    }

    @Test
    void executeQuery_updateQuery_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.executeQuery("UPDATE material SET name='x'"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void executeQuery_deleteQuery_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.executeQuery("DELETE FROM material"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void executeQuery_emptyResult_returnsEmptyDto() {
        when(jdbcTemplate.queryForList("SELECT * FROM formula")).thenReturn(List.of());
        SqlResultDto result = service.executeQuery("SELECT * FROM formula");
        assertThat(result.columns()).isEmpty();
        assertThat(result.rows()).isEmpty();
        assertThat(result.rowCount()).isEqualTo(0);
    }

    @Test
    void executeQuery_nullValue_preservedAsNull() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("description", null);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row));

        SqlResultDto result = service.executeQuery("SELECT id, description FROM material");

        assertThat(result.rows().get(0).get("description")).isNull();
    }

    @Test
    void executeQuery_over1000Rows_capsAt1000() {
        List<Map<String, Object>> bigList = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", i);
            bigList.add(r);
        }
        when(jdbcTemplate.queryForList(anyString())).thenReturn(bigList);

        SqlResultDto result = service.executeQuery("SELECT id FROM material");

        assertThat(result.rowCount()).isEqualTo(1000);
        assertThat(result.rows()).hasSize(1000);
    }

    // ── getSchema – tables ────────────────────────────────────────────────────

    @Test
    void getSchema_groupsColumnsByTable() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(
                        schemaRow("material", "id", "integer", "NO"),
                        schemaRow("material", "name", "character varying", "NO"),
                        schemaRow("formula", "id", "integer", "NO")
                ))
                .thenReturn(List.of());

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.tables()).hasSize(2);
        assertThat(schema.tables().get(0).name()).isEqualTo("material");
        assertThat(schema.tables().get(0).columns()).hasSize(2);
        assertThat(schema.tables().get(1).name()).isEqualTo("formula");
    }

    @Test
    void getSchema_nullableFlag_correctlyMapped() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(
                        schemaRow("material", "name", "character varying", "NO"),
                        schemaRow("material", "description", "text", "YES")
                ))
                .thenReturn(List.of());

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.tables().get(0).columns().get(0).nullable()).isFalse();
        assertThat(schema.tables().get(0).columns().get(1).nullable()).isTrue();
    }

    // ── getSchema – foreign keys ──────────────────────────────────────────────

    @Test
    void getSchema_returnsForeignKeys() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(schemaRow("material", "id", "integer", "NO")))
                .thenReturn(List.of(fkRow("material", "group_id", "material_group", "id")));

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.foreignKeys()).hasSize(1);
        assertThat(schema.foreignKeys().get(0).fromTable()).isEqualTo("material");
        assertThat(schema.foreignKeys().get(0).fromColumn()).isEqualTo("group_id");
        assertThat(schema.foreignKeys().get(0).toTable()).isEqualTo("material_group");
        assertThat(schema.foreignKeys().get(0).toColumn()).isEqualTo("id");
    }

    @Test
    void getSchema_multipleForeignKeys_allReturned() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of())
                .thenReturn(List.of(
                        fkRow("material", "group_id", "material_group", "id"),
                        fkRow("formula", "group_id", "formula_group", "id")
                ));

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.foreignKeys()).hasSize(2);
    }

    @Test
    void getSchema_noForeignKeys_returnsEmptyList() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of())
                .thenReturn(List.of());

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.foreignKeys()).isEmpty();
    }

    @Test
    void getSchema_emptyDatabase_returnsEmptySchema() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of())
                .thenReturn(List.of());

        SqlSchemaDto schema = service.getSchema();

        assertThat(schema.tables()).isEmpty();
        assertThat(schema.foreignKeys()).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> schemaRow(String table, String column, String type, String nullable) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("table_name", table);
        row.put("column_name", column);
        row.put("data_type", type);
        row.put("is_nullable", nullable);
        return row;
    }

    private Map<String, Object> fkRow(String fromTable, String fromCol, String toTable, String toCol) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("from_table", fromTable);
        row.put("from_column", fromCol);
        row.put("to_table", toTable);
        row.put("to_column", toCol);
        return row;
    }
}
