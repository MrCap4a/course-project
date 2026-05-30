package ru.denis.Calculator.Control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FormulaControllerTest {

    @Mock IFormulaService formulaService;
    @Mock IFormulaGroupService formulaGroupService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FormulaController(formulaService, formulaGroupService))
                .build();
    }

    // ── GET /formulas ─────────────────────────────────────────────────────────

    @Test
    void getAllFormulas_returns200WithList() throws Exception {
        when(formulaService.getAllFormulas())
                .thenReturn(List.of(new FormulaDto(1, "Area", "{const}*{const}", 1, "Geometry")));

        mockMvc.perform(get("/formulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Area"))
                .andExpect(jsonPath("$[0].expression").value("{const}*{const}"))
                .andExpect(jsonPath("$[0].groupId").value(1))
                .andExpect(jsonPath("$[0].groupName").value("Geometry"));
    }

    @Test
    void getAllFormulas_empty_returnsEmptyList() throws Exception {
        when(formulaService.getAllFormulas()).thenReturn(List.of());

        mockMvc.perform(get("/formulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /formulas ────────────────────────────────────────────────────────

    @Test
    void createFormula_validRequest_returns201() throws Exception {
        when(formulaService.createFormula(any()))
                .thenReturn(new FormulaDto(5, "Volume", "{const}*{const}*{const}", 1, "Geometry"));

        mockMvc.perform(post("/formulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Volume\",\"expression\":\"{const}*{const}*{const}\",\"groupId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Volume"));
    }

    @Test
    void createFormula_groupNotFound_propagatesException() {
        when(formulaService.createFormula(any()))
                .thenThrow(new RuntimeException("FormulaGroup not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(post("/formulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"F\",\"expression\":\"1\",\"groupId\":99}")))
                .hasRootCauseMessage("FormulaGroup not found: 99");
    }

    // ── PUT /formulas/{id} ────────────────────────────────────────────────────

    @Test
    void editFormula_validRequest_returns200() throws Exception {
        when(formulaService.editFormula(eq(1), any()))
                .thenReturn(new FormulaDto(1, "Updated", "{material}*2", 1, "Geometry"));

        mockMvc.perform(put("/formulas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"expression\":\"{material}*2\",\"groupId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.expression").value("{material}*2"));
    }

    @Test
    void editFormula_notFound_propagatesException() {
        when(formulaService.editFormula(eq(99), any()))
                .thenThrow(new RuntimeException("Formula not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/formulas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"F\",\"expression\":\"1\",\"groupId\":1}")))
                .hasRootCauseMessage("Formula not found: 99");
    }

    // ── DELETE /formulas/{id} ─────────────────────────────────────────────────

    @Test
    void deleteFormula_exists_returns204() throws Exception {
        mockMvc.perform(delete("/formulas/1"))
                .andExpect(status().isNoContent());

        verify(formulaService).deleteFormula(1);
    }

    @Test
    void deleteFormula_notFound_propagatesException() {
        doThrow(new RuntimeException("Formula not found: 99"))
                .when(formulaService).deleteFormula(99);

        assertThatThrownBy(() -> mockMvc.perform(delete("/formulas/99")))
                .hasRootCauseMessage("Formula not found: 99");
    }

    // ── GET /formula-groups ───────────────────────────────────────────────────

    @Test
    void getAllFormulaGroups_returns200WithList() throws Exception {
        when(formulaGroupService.getAllFormulaGroups())
                .thenReturn(List.of(new FormulaGroupDto(1, "Geometry"), new FormulaGroupDto(2, "Finance")));

        mockMvc.perform(get("/formula-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Geometry"))
                .andExpect(jsonPath("$[1].name").value("Finance"));
    }

    // ── POST /formula-groups ──────────────────────────────────────────────────

    @Test
    void createFormulaGroup_validRequest_returns201() throws Exception {
        when(formulaGroupService.createFormulaGroup(any()))
                .thenReturn(new FormulaGroupDto(3, "Physics"));

        mockMvc.perform(post("/formula-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Physics\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Physics"));
    }

    // ── PUT /formula-groups/{id} ──────────────────────────────────────────────

    @Test
    void editFormulaGroup_validRequest_returns200() throws Exception {
        when(formulaGroupService.editFormulaGroup(eq(1), any()))
                .thenReturn(new FormulaGroupDto(1, "Renamed"));

        mockMvc.perform(put("/formula-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));
    }

    // ── DELETE /formula-groups/{id} ───────────────────────────────────────────

    @Test
    void deleteFormulaGroup_exists_returns204() throws Exception {
        mockMvc.perform(delete("/formula-groups/1"))
                .andExpect(status().isNoContent());

        verify(formulaGroupService).deleteFormulaGroup(1);
    }

    @Test
    void deleteFormulaGroup_notFound_propagatesException() {
        doThrow(new RuntimeException("FormulaGroup not found: 99"))
                .when(formulaGroupService).deleteFormulaGroup(99);

        assertThatThrownBy(() -> mockMvc.perform(delete("/formula-groups/99")))
                .hasRootCauseMessage("FormulaGroup not found: 99");
    }
}
