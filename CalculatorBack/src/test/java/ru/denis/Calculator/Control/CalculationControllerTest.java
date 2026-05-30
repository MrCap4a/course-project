package ru.denis.Calculator.Control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.math.BigDecimal;
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
class CalculationControllerTest {

    @Mock ICalculationService calculationService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CalculationController(calculationService))
                .build();
    }

    private CalculationDto sampleDto(int id, String name) {
        return new CalculationDto(id, name, 1, "Area", "{const}*2", List.of(), new BigDecimal("50"));
    }

    // ── GET /calculations ─────────────────────────────────────────────────────

    @Test
    void getAllCalculations_returns200WithList() throws Exception {
        when(calculationService.getAllCalculations())
                .thenReturn(List.of(sampleDto(1, "Roof"), sampleDto(2, "Floor")));

        mockMvc.perform(get("/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Roof"))
                .andExpect(jsonPath("$[0].formulaId").value(1))
                .andExpect(jsonPath("$[0].result").value(50))
                .andExpect(jsonPath("$[1].name").value("Floor"));
    }

    @Test
    void getAllCalculations_empty_returnsEmptyList() throws Exception {
        when(calculationService.getAllCalculations()).thenReturn(List.of());

        mockMvc.perform(get("/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /calculations/{id} ────────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(calculationService.getCalculationById(1)).thenReturn(sampleDto(1, "Roof"));

        mockMvc.perform(get("/calculations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Roof"))
                .andExpect(jsonPath("$.formulaExpression").value("{const}*2"))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getById_notFound_propagatesException() {
        when(calculationService.getCalculationById(99))
                .thenThrow(new RuntimeException("Calculation not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(get("/calculations/99")))
                .hasRootCauseMessage("Calculation not found: 99");
    }

    // ── POST /calculations ────────────────────────────────────────────────────

    @Test
    void createCalculation_validRequest_returns201() throws Exception {
        when(calculationService.createCalculation(any())).thenReturn(sampleDto(5, "NewCalc"));

        mockMvc.perform(post("/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewCalc\",\"formulaId\":1,\"items\":[]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("NewCalc"));
    }

    @Test
    void createCalculation_formulaNotFound_propagatesException() {
        when(calculationService.createCalculation(any()))
                .thenThrow(new RuntimeException("Formula not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(post("/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"C\",\"formulaId\":99,\"items\":[]}")))
                .hasRootCauseMessage("Formula not found: 99");
    }

    @Test
    void createCalculation_withItems_parsesBodyCorrectly() throws Exception {
        when(calculationService.createCalculation(any())).thenReturn(sampleDto(5, "WithItems"));

        mockMvc.perform(post("/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "WithItems",
                              "formulaId": 1,
                              "items": [
                                {"position": 1, "quantity": 5.0, "materialId": 10},
                                {"position": 2, "quantity": 2.0, "materialId": null}
                              ]
                            }
                            """))
                .andExpect(status().isCreated());
    }

    // ── PUT /calculations/{id} ────────────────────────────────────────────────

    @Test
    void editCalculation_validRequest_returns200() throws Exception {
        when(calculationService.editCalculation(eq(1), any()))
                .thenReturn(sampleDto(1, "Updated"));

        mockMvc.perform(put("/calculations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"formulaId\":1,\"items\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void editCalculation_notFound_propagatesException() {
        when(calculationService.editCalculation(eq(99), any()))
                .thenThrow(new RuntimeException("Calculation not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/calculations/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"formulaId\":1,\"items\":[]}")))
                .hasRootCauseMessage("Calculation not found: 99");
    }

    // ── DELETE /calculations/{id} ─────────────────────────────────────────────

    @Test
    void deleteCalculation_exists_returns204() throws Exception {
        mockMvc.perform(delete("/calculations/1"))
                .andExpect(status().isNoContent());

        verify(calculationService).deleteCalculation(1);
    }

    @Test
    void deleteCalculation_notFound_propagatesException() {
        doThrow(new RuntimeException("Calculation not found: 99"))
                .when(calculationService).deleteCalculation(99);

        assertThatThrownBy(() -> mockMvc.perform(delete("/calculations/99")))
                .hasRootCauseMessage("Calculation not found: 99");
    }
}
