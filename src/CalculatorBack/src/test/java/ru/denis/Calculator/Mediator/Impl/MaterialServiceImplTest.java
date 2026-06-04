package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock private MaterialRepository materialRepository;
    @Mock private MaterialGroupRepository materialGroupRepository;
    @InjectMocks private MaterialServiceImpl service;

    // ── getAllMaterials ───────────────────────────────────────────────────────

    @Test
    void getAllMaterials_noFilters_returnsFindAll() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(material(1, "Steel", new BigDecimal("10"), "kg", g))));

        var result = service.getAllMaterials(null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Steel");
        verify(materialRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllMaterials_groupOnly_findsByGroup() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(eq(g), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(material(1, "Steel", new BigDecimal("10"), "kg", g))));

        var result = service.getAllMaterials(1, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findByGroup(eq(g), any(Pageable.class));
    }

    @Test
    void getAllMaterials_searchOnly_findsByName() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findByNameContainingIgnoreCase(eq("ste"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(material(1, "Steel", new BigDecimal("10"), "kg", g))));

        var result = service.getAllMaterials(null, "ste", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findByNameContainingIgnoreCase(eq("ste"), any(Pageable.class));
    }

    @Test
    void getAllMaterials_groupAndSearch_findsByGroupAndName() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroupAndNameContainingIgnoreCase(eq(g), eq("ste"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(material(1, "Steel", new BigDecimal("10"), "kg", g))));

        var result = service.getAllMaterials(1, "ste", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findByGroupAndNameContainingIgnoreCase(eq(g), eq("ste"), any(Pageable.class));
    }

    @Test
    void getAllMaterials_groupNotFound_throws() {
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllMaterials(99, "x", Pageable.unpaged()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    @Test
    void getAllMaterials_blankSearch_treatedAsNoSearch() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(material(1, "Steel", new BigDecimal("10"), "kg", g))));

        var result = service.getAllMaterials(null, "   ", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findAll(any(Pageable.class));
    }

    // ── getMaterialById ───────────────────────────────────────────────────────

    @Test
    void getMaterialById_found_returnsDto() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findById(1)).thenReturn(Optional.of(material(1, "Steel", new BigDecimal("10"), "kg", g)));

        MaterialDto dto = service.getMaterialById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("Steel");
        assertThat(dto.price()).isEqualByComparingTo("10");
        assertThat(dto.units()).isEqualTo("kg");
        assertThat(dto.groupId()).isEqualTo(1);
        assertThat(dto.groupName()).isEqualTo("G1");
    }

    @Test
    void getMaterialById_notFound_throws() {
        when(materialRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMaterialById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Material not found: 99");
    }

    // ── createMaterial ────────────────────────────────────────────────────────

    @Test
    void createMaterial_success_savesAndReturnsDto() {
        MaterialGroup g = group(1, "G1");
        MaterialRequest req = new MaterialRequest("Steel", new BigDecimal("10"), "kg", 1);
        Material saved = material(5, "Steel", new BigDecimal("10"), "kg", g);
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.save(any())).thenReturn(saved);

        MaterialDto dto = service.createMaterial(req);

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.name()).isEqualTo("Steel");
        verify(materialRepository).save(any());
    }

    @Test
    void createMaterial_groupNotFound_throws() {
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMaterial(new MaterialRequest("X", BigDecimal.ONE, "u", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    // ── editMaterial ──────────────────────────────────────────────────────────

    @Test
    void editMaterial_success_updatesAndReturnsDto() {
        MaterialGroup g = group(1, "G1");
        Material existing = material(1, "Old", new BigDecimal("5"), "m", g);
        Material updated = material(1, "New", new BigDecimal("15"), "kg", g);
        when(materialRepository.findById(1)).thenReturn(Optional.of(existing));
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.save(existing)).thenReturn(updated);

        MaterialDto dto = service.editMaterial(1, new MaterialRequest("New", new BigDecimal("15"), "kg", 1));

        assertThat(dto.name()).isEqualTo("New");
        assertThat(dto.price()).isEqualByComparingTo("15");
    }

    @Test
    void editMaterial_materialNotFound_throws() {
        when(materialRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editMaterial(99, new MaterialRequest("X", BigDecimal.ONE, "u", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Material not found: 99");
    }

    @Test
    void editMaterial_groupNotFound_throws() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findById(1)).thenReturn(Optional.of(material(1, "X", BigDecimal.ONE, "u", g)));
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editMaterial(1, new MaterialRequest("X", BigDecimal.ONE, "u", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    // ── deleteMaterial ────────────────────────────────────────────────────────

    @Test
    void deleteMaterial_found_callsDeleteById() {
        MaterialGroup g = group(1, "G1");
        when(materialRepository.findById(1)).thenReturn(Optional.of(material(1, "Steel", BigDecimal.ONE, "kg", g)));

        service.deleteMaterial(1);

        verify(materialRepository).deleteById(1);
    }

    @Test
    void deleteMaterial_notFound_throws() {
        when(materialRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMaterial(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Material not found: 99");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MaterialGroup group(int id, String name) {
        MaterialGroup g = new MaterialGroup();
        g.setId(id);
        g.setName(name);
        return g;
    }

    private Material material(int id, String name, BigDecimal price, String units, MaterialGroup group) {
        Material m = new Material();
        m.setId(id);
        m.setName(name);
        m.setPrice(price);
        m.setUnits(units);
        m.setGroup(group);
        return m;
    }
}
