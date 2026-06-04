package ru.denis.calculatordesktop.util;

import org.junit.jupiter.api.Test;
import ru.denis.calculatordesktop.api.dto.SqlSchemaDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqlDiagramBuilderTest {

    // ── buildErDiagram ────────────────────────────────────────────────────────

    @Test
    void buildErDiagram_startsWithErDiagram() {
        SqlSchemaDto schema = new SqlSchemaDto(List.of(), List.of());
        assertThat(SqlDiagramBuilder.buildErDiagram(schema)).startsWith("erDiagram\n");
    }

    @Test
    void buildErDiagram_includesTableName() {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("id", "integer", false);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("material", List.of(col));
        SqlSchemaDto schema = new SqlSchemaDto(List.of(table), List.of());

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("material {");
    }

    @Test
    void buildErDiagram_includesColumnWithType() {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("name", "character varying", false);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("material", List.of(col));
        SqlSchemaDto schema = new SqlSchemaDto(List.of(table), List.of());

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("character_varying name \"required\"");
    }

    @Test
    void buildErDiagram_nullableColumnMarkedNullable() {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("description", "text", true);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("material", List.of(col));
        SqlSchemaDto schema = new SqlSchemaDto(List.of(table), List.of());

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("text description \"nullable\"");
    }

    @Test
    void buildErDiagram_typeSpacesReplacedWithUnderscore() {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("ts", "timestamp without time zone", false);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("audit_log", List.of(col));
        SqlSchemaDto schema = new SqlSchemaDto(List.of(table), List.of());

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("timestamp_without_time_zone ts");
    }

    @Test
    void buildErDiagram_includesForeignKeyRelation() {
        SqlSchemaDto.ForeignKey fk = new SqlSchemaDto.ForeignKey("material", "group_id", "material_group", "id");
        SqlSchemaDto schema = new SqlSchemaDto(List.of(), List.of(fk));

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("material }o--|| material_group : \"group_id\"");
    }

    @Test
    void buildErDiagram_duplicateForeignKeysBetweenSameTables_appearsOnce() {
        SqlSchemaDto.ForeignKey fk1 = new SqlSchemaDto.ForeignKey("material", "group_id", "material_group", "id");
        SqlSchemaDto.ForeignKey fk2 = new SqlSchemaDto.ForeignKey("material", "group_id", "material_group", "id");
        SqlSchemaDto schema = new SqlSchemaDto(List.of(), List.of(fk1, fk2));

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        long count = result.lines()
                .filter(l -> l.contains("material }o--|| material_group"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void buildErDiagram_multipleTables_allIncluded() {
        SqlSchemaDto.TableInfo t1 = new SqlSchemaDto.TableInfo("user",
                List.of(new SqlSchemaDto.ColumnInfo("id", "integer", false)));
        SqlSchemaDto.TableInfo t2 = new SqlSchemaDto.TableInfo("formula",
                List.of(new SqlSchemaDto.ColumnInfo("id", "integer", false)));
        SqlSchemaDto schema = new SqlSchemaDto(List.of(t1, t2), List.of());

        String result = SqlDiagramBuilder.buildErDiagram(schema);

        assertThat(result).contains("user {").contains("formula {");
    }

    @Test
    void buildErDiagram_emptySchema_returnsOnlyHeader() {
        SqlSchemaDto schema = new SqlSchemaDto(List.of(), List.of());
        assertThat(SqlDiagramBuilder.buildErDiagram(schema)).isEqualToIgnoringWhitespace("erDiagram");
    }

    // ── escapeForHtml ─────────────────────────────────────────────────────────

    @Test
    void escapeForHtml_escapesAmpersand() {
        assertThat(SqlDiagramBuilder.escapeForHtml("a & b")).isEqualTo("a &amp; b");
    }

    @Test
    void escapeForHtml_escapesLessThan() {
        assertThat(SqlDiagramBuilder.escapeForHtml("a < b")).isEqualTo("a &lt; b");
    }

    @Test
    void escapeForHtml_escapesGreaterThan() {
        assertThat(SqlDiagramBuilder.escapeForHtml("a > b")).isEqualTo("a &gt; b");
    }

    @Test
    void escapeForHtml_noSpecialChars_unchanged() {
        assertThat(SqlDiagramBuilder.escapeForHtml("erDiagram\n  user {\n  }"))
                .isEqualTo("erDiagram\n  user {\n  }");
    }

    @Test
    void escapeForHtml_multipleSpecialChars_allEscaped() {
        assertThat(SqlDiagramBuilder.escapeForHtml("<b>a & b</b>"))
                .isEqualTo("&lt;b&gt;a &amp; b&lt;/b&gt;");
    }
}
