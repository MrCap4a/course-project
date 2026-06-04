package ru.denis.Calculator.Control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.denis.Calculator.Dto.SqlResultDto;
import ru.denis.Calculator.Dto.SqlSchemaDto;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Impl.SqlServiceImpl;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SqlControllerTest {

    @Mock SqlServiceImpl sqlService;
    @Mock PermissionChecker checker;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SqlController(sqlService, checker))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── POST /sql/execute ─────────────────────────────────────────────────────

    @Test
    void execute_validSelect_returns200WithResult() throws Exception {
        SqlResultDto dto = new SqlResultDto(
<<<<<<< Updated upstream
                List.of("id", "name"),
                List.of(Map.of("id", "1", "name", "Alice")),
                1
        );
=======
                List.of("id", "name"), List.of(Map.of("id", "1", "name", "Alice")), 1);
>>>>>>> Stashed changes
        when(sqlService.executeQuery(anyString())).thenReturn(dto);

        mockMvc.perform(post("/sql/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT * FROM \\\"user\\\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rowCount").value(1))
                .andExpect(jsonPath("$.columns[0]").value("id"))
<<<<<<< Updated upstream
                .andExpect(jsonPath("$.columns[1]").value("name"))
=======
>>>>>>> Stashed changes
                .andExpect(jsonPath("$.rows[0].name").value("Alice"));
    }

    @Test
    void execute_emptyResult_returns200WithEmptyDto() throws Exception {
<<<<<<< Updated upstream
        SqlResultDto dto = new SqlResultDto(List.of(), List.of(), 0);
        when(sqlService.executeQuery(anyString())).thenReturn(dto);
=======
        when(sqlService.executeQuery(anyString())).thenReturn(new SqlResultDto(List.of(), List.of(), 0));
>>>>>>> Stashed changes

        mockMvc.perform(post("/sql/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isOk())
<<<<<<< Updated upstream
                .andExpect(jsonPath("$.rowCount").value(0))
                .andExpect(jsonPath("$.columns").isEmpty());
    }

    @Test
    void execute_nonSelectQuery_returns400() throws Exception {
=======
                .andExpect(jsonPath("$.rowCount").value(0));
    }

    @Test
    void execute_nonSelectQuery_returns400WithError() throws Exception {
>>>>>>> Stashed changes
        when(sqlService.executeQuery(anyString()))
                .thenThrow(new IllegalArgumentException("Разрешены только SELECT-запросы"));

        mockMvc.perform(post("/sql/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"DELETE FROM \\\"user\\\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Разрешены только SELECT-запросы"));
    }

    @Test
<<<<<<< Updated upstream
    void execute_requiresPermission() throws Exception {
=======
    void execute_callsPermissionChecker() throws Exception {
        when(sqlService.executeQuery(anyString())).thenReturn(new SqlResultDto(List.of(), List.of(), 0));

>>>>>>> Stashed changes
        mockMvc.perform(post("/sql/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isOk());

        verify(checker).require("sql.execute");
    }

<<<<<<< Updated upstream
    // ── GET /sql/schema ───────────────────────────────────────────────────────

    @Test
    void schema_returns200WithSchemaDto() throws Exception {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("id", "integer", false);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("material", List.of(col));
        SqlSchemaDto dto = new SqlSchemaDto(List.of(table));
=======
    @Test
    void execute_accessDenied_returns403() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Missing permission: sql.execute"))
                .when(checker).require("sql.execute");

        mockMvc.perform(post("/sql/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Missing permission: sql.execute"));
    }

    // ── GET /sql/schema ───────────────────────────────────────────────────────

    @Test
    void schema_returns200WithTablesAndForeignKeys() throws Exception {
        SqlSchemaDto.ColumnInfo col = new SqlSchemaDto.ColumnInfo("id", "integer", false);
        SqlSchemaDto.TableInfo table = new SqlSchemaDto.TableInfo("material", List.of(col));
        SqlSchemaDto.ForeignKey fk = new SqlSchemaDto.ForeignKey("material", "group_id", "material_group", "id");
        SqlSchemaDto dto = new SqlSchemaDto(List.of(table), List.of(fk));
>>>>>>> Stashed changes
        when(sqlService.getSchema()).thenReturn(dto);

        mockMvc.perform(get("/sql/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].name").value("material"))
                .andExpect(jsonPath("$.tables[0].columns[0].name").value("id"))
<<<<<<< Updated upstream
                .andExpect(jsonPath("$.tables[0].columns[0].type").value("integer"))
                .andExpect(jsonPath("$.tables[0].columns[0].nullable").value(false));
    }

    @Test
    void schema_emptyDatabase_returnsEmptyList() throws Exception {
        when(sqlService.getSchema()).thenReturn(new SqlSchemaDto(List.of()));

        mockMvc.perform(get("/sql/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables").isEmpty());
    }

    @Test
    void schema_requiresPermission() throws Exception {
        when(sqlService.getSchema()).thenReturn(new SqlSchemaDto(List.of()));

        mockMvc.perform(get("/sql/schema"))
                .andExpect(status().isOk());

        verify(checker).require("sql.execute");
    }

    @Test
    void schema_accessDenied_returns403() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Missing permission: sql.execute"))
                .when(checker).require("sql.execute");

        mockMvc.perform(get("/sql/schema"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Missing permission: sql.execute"));
    }
=======
                .andExpect(jsonPath("$.foreignKeys[0].fromTable").value("material"))
                .andExpect(jsonPath("$.foreignKeys[0].toTable").value("material_group"));
    }

    @Test
    void schema_emptyDatabase_returnsEmptyLists() throws Exception {
        when(sqlService.getSchema()).thenReturn(new SqlSchemaDto(List.of(), List.of()));

        mockMvc.perform(get("/sql/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables").isEmpty())
                .andExpect(jsonPath("$.foreignKeys").isEmpty());
    }

    @Test
    void schema_callsPermissionChecker() throws Exception {
        when(sqlService.getSchema()).thenReturn(new SqlSchemaDto(List.of(), List.of()));

        mockMvc.perform(get("/sql/schema")).andExpect(status().isOk());

        verify(checker).require("sql.execute");
    }
>>>>>>> Stashed changes
}
