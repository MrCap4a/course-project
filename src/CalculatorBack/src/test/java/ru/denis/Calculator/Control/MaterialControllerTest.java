package ru.denis.Calculator.Control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MaterialControllerTest {

    @Mock IMaterialService materialService;
    @Mock IMaterialGroupService materialGroupService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MaterialController(materialService, materialGroupService))
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ── GET /materials ────────────────────────────────────────────────────────

    @Test
    void getAllMaterials_noFilters_returns200WithList() throws Exception {
        var items = List.of(new MaterialDto(1, "Steel", new BigDecimal("10"), "kg", 1, "G1"));
        when(materialService.getAllMaterials(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(items, PageRequest.of(0, 20), items.size()));

        mockMvc.perform(get("/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Steel"))
                .andExpect(jsonPath("$.content[0].price").value(10))
                .andExpect(jsonPath("$.content[0].units").value("kg"))
                .andExpect(jsonPath("$.content[0].groupId").value(1))
                .andExpect(jsonPath("$.content[0].groupName").value("G1"));
    }

    @Test
    void getAllMaterials_withGroupFilter_passesParamsToService() throws Exception {
        when(materialService.getAllMaterials(eq(2), isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/materials").param("groupId", "2"))
                .andExpect(status().isOk());

        verify(materialService).getAllMaterials(eq(2), isNull(), any(Pageable.class));
    }

    @Test
    void getAllMaterials_withSearchFilter_passesParamsToService() throws Exception {
        when(materialService.getAllMaterials(isNull(), eq("ste"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/materials").param("search", "ste"))
                .andExpect(status().isOk());

        verify(materialService).getAllMaterials(isNull(), eq("ste"), any(Pageable.class));
    }

    @Test
    void getAllMaterials_withBothFilters_passesParamsToService() throws Exception {
        when(materialService.getAllMaterials(eq(1), eq("steel"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/materials").param("groupId", "1").param("search", "steel"))
                .andExpect(status().isOk());

        verify(materialService).getAllMaterials(eq(1), eq("steel"), any(Pageable.class));
    }

    @Test
    void getAllMaterials_serviceThrows_propagatesException() {
        when(materialService.getAllMaterials(isNull(), isNull(), any(Pageable.class)))
                .thenThrow(new RuntimeException("unexpected error"));

        assertThatThrownBy(() -> mockMvc.perform(get("/materials")))
                .hasRootCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("unexpected error");
    }

    // ── POST /materials ───────────────────────────────────────────────────────

    @Test
    void createMaterial_validRequest_returns201WithBody() throws Exception {
        when(materialService.createMaterial(any()))
                .thenReturn(new MaterialDto(5, "Steel", new BigDecimal("10"), "kg", 1, "G1"));

        mockMvc.perform(post("/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Steel\",\"price\":10,\"units\":\"kg\",\"groupId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Steel"));
    }

    @Test
    void createMaterial_groupNotFound_propagatesException() {
        when(materialService.createMaterial(any()))
                .thenThrow(new RuntimeException("MaterialGroup not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(post("/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Steel\",\"price\":10,\"units\":\"kg\",\"groupId\":99}")))
                .hasRootCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("MaterialGroup not found: 99");
    }

    // ── PUT /materials/{id} ───────────────────────────────────────────────────

    @Test
    void editMaterial_validRequest_returns200WithBody() throws Exception {
        when(materialService.editMaterial(eq(1), any()))
                .thenReturn(new MaterialDto(1, "StainlessSteel", new BigDecimal("15"), "kg", 1, "G1"));

        mockMvc.perform(put("/materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"StainlessSteel\",\"price\":15,\"units\":\"kg\",\"groupId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("StainlessSteel"))
                .andExpect(jsonPath("$.price").value(15));
    }

    @Test
    void editMaterial_notFound_propagatesException() {
        when(materialService.editMaterial(eq(99), any()))
                .thenThrow(new RuntimeException("Material not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/materials/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"price\":1,\"units\":\"u\",\"groupId\":1}")))
                .hasRootCauseMessage("Material not found: 99");
    }

    // ── DELETE /materials/{id} ────────────────────────────────────────────────

    @Test
    void deleteMaterial_exists_returns204() throws Exception {
        mockMvc.perform(delete("/materials/1"))
                .andExpect(status().isNoContent());

        verify(materialService).deleteMaterial(1);
    }

    @Test
    void deleteMaterial_notFound_propagatesException() {
        doThrow(new RuntimeException("Material not found: 99"))
                .when(materialService).deleteMaterial(99);

        assertThatThrownBy(() -> mockMvc.perform(delete("/materials/99")))
                .hasRootCauseMessage("Material not found: 99");
    }

    // ── GET /material-groups ──────────────────────────────────────────────────

    @Test
    void getAllMaterialGroups_returns200WithList() throws Exception {
        when(materialGroupService.getAllMaterialGroups())
                .thenReturn(List.of(new MaterialGroupDto(1, "Metals"), new MaterialGroupDto(2, "Wood")));

        mockMvc.perform(get("/material-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Metals"))
                .andExpect(jsonPath("$[1].name").value("Wood"));
    }

    // ── POST /material-groups ─────────────────────────────────────────────────

    @Test
    void createMaterialGroup_validRequest_returns201() throws Exception {
        when(materialGroupService.createMaterialGroup(any()))
                .thenReturn(new MaterialGroupDto(3, "Plastics"));

        mockMvc.perform(post("/material-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Plastics\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Plastics"));
    }

    // ── PUT /material-groups/{id} ─────────────────────────────────────────────

    @Test
    void editMaterialGroup_validRequest_returns200() throws Exception {
        when(materialGroupService.editMaterialGroup(eq(1), any()))
                .thenReturn(new MaterialGroupDto(1, "Updated"));

        mockMvc.perform(put("/material-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // ── DELETE /material-groups/{id} ──────────────────────────────────────────

    @Test
    void deleteMaterialGroup_cascade_returns204() throws Exception {
        mockMvc.perform(delete("/material-groups/1")
                        .param("strategy", "CASCADE"))
                .andExpect(status().isNoContent());

        verify(materialGroupService).deleteMaterialGroup(1, DeleteGroupStrategy.CASCADE, null);
    }

    @Test
    void deleteMaterialGroup_move_passesTargetGroupId() throws Exception {
        mockMvc.perform(delete("/material-groups/1")
                        .param("strategy", "MOVE")
                        .param("targetGroupId", "2"))
                .andExpect(status().isNoContent());

        verify(materialGroupService).deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 2);
    }
}
