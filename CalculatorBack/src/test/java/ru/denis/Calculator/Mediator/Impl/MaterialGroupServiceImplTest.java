package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialGroupServiceImplTest {

    @Mock private MaterialGroupRepository materialGroupRepository;
    @Mock private MaterialRepository materialRepository;
    @InjectMocks private MaterialGroupServiceImpl service;

    // ── getAllMaterialGroups ──────────────────────────────────────────────────

    @Test
    void getAllMaterialGroups_returnsMappedList() {
        when(materialGroupRepository.findAll()).thenReturn(List.of(group(1, "G1"), group(2, "G2")));

        List<MaterialGroupDto> result = service.getAllMaterialGroups();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MaterialGroupDto::name).containsExactly("G1", "G2");
    }

    // ── getMaterialGroupById ─────────────────────────────────────────────────

    @Test
    void getMaterialGroupById_found_returnsDto() {
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(group(1, "G1")));

        MaterialGroupDto dto = service.getMaterialGroupById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("G1");
    }

    @Test
    void getMaterialGroupById_notFound_throws() {
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMaterialGroupById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    // ── createMaterialGroup ──────────────────────────────────────────────────

    @Test
    void createMaterialGroup_savesAndReturnsDto() {
        when(materialGroupRepository.save(any())).thenReturn(group(5, "NewGroup"));

        MaterialGroupDto dto = service.createMaterialGroup(new MaterialGroupRequest("NewGroup"));

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.name()).isEqualTo("NewGroup");
        verify(materialGroupRepository).save(any());
    }

    // ── editMaterialGroup ────────────────────────────────────────────────────

    @Test
    void editMaterialGroup_found_updatesNameAndReturns() {
        MaterialGroup g = group(1, "Old");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialGroupRepository.save(g)).thenReturn(group(1, "New"));

        MaterialGroupDto dto = service.editMaterialGroup(1, new MaterialGroupRequest("New"));

        assertThat(dto.name()).isEqualTo("New");
    }

    @Test
    void editMaterialGroup_notFound_throws() {
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editMaterialGroup(99, new MaterialGroupRequest("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    // ── deleteMaterialGroup — CASCADE ────────────────────────────────────────

    @Test
    void deleteMaterialGroup_cascade_deletesAllMaterialsThenGroup() {
        MaterialGroup g = group(1, "G1");
        Material m = material(1, g);
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of(m));

        service.deleteMaterialGroup(1, DeleteGroupStrategy.CASCADE, null);

        verify(materialRepository).deleteAll(List.of(m));
        verify(materialGroupRepository).delete(g);
    }

    @Test
    void deleteMaterialGroup_cascade_emptyGroup_deletesGroup() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of());

        service.deleteMaterialGroup(1, DeleteGroupStrategy.CASCADE, null);

        verify(materialRepository).deleteAll(List.of());
        verify(materialGroupRepository).delete(g);
    }

    // ── deleteMaterialGroup — DEFAULT ────────────────────────────────────────

    @Test
    void deleteMaterialGroup_default_movesToExistingDefaultGroup() {
        MaterialGroup g = group(1, "G1");
        MaterialGroup defaultGroup = group(2, "Без группы");
        Material m = material(1, g);
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of(m));
        when(materialGroupRepository.findByName("Без группы")).thenReturn(Optional.of(defaultGroup));

        service.deleteMaterialGroup(1, DeleteGroupStrategy.DEFAULT, null);

        assertThat(m.getGroup()).isEqualTo(defaultGroup);
        verify(materialRepository).saveAll(List.of(m));
        verify(materialGroupRepository).delete(g);
    }

    @Test
    void deleteMaterialGroup_default_createsDefaultGroupWhenAbsent() {
        MaterialGroup g = group(1, "G1");
        MaterialGroup newDefault = group(99, "Без группы");
        Material m = material(1, g);
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of(m));
        when(materialGroupRepository.findByName("Без группы")).thenReturn(Optional.empty());
        when(materialGroupRepository.save(any())).thenReturn(newDefault);

        service.deleteMaterialGroup(1, DeleteGroupStrategy.DEFAULT, null);

        verify(materialGroupRepository, atLeastOnce()).save(any());
        verify(materialGroupRepository).delete(g);
    }

    // ── deleteMaterialGroup — MOVE ───────────────────────────────────────────

    @Test
    void deleteMaterialGroup_move_movesToTargetGroup() {
        MaterialGroup g = group(1, "G1");
        MaterialGroup target = group(2, "G2");
        Material m = material(1, g);
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialGroupRepository.findById(2)).thenReturn(Optional.of(target));
        when(materialRepository.findByGroup(g)).thenReturn(List.of(m));

        service.deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 2);

        assertThat(m.getGroup()).isEqualTo(target);
        verify(materialRepository).saveAll(List.of(m));
        verify(materialGroupRepository).delete(g);
    }

    @Test
    void deleteMaterialGroup_move_nullTargetId_throws() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of());

        assertThatThrownBy(() -> service.deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("targetGroupId is required");
    }

    @Test
    void deleteMaterialGroup_move_targetSameAsDeleted_throws() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of());

        assertThatThrownBy(() -> service.deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot move materials to the group being deleted");
    }

    @Test
    void deleteMaterialGroup_move_targetGroupNotFound_throws() {
        MaterialGroup g = group(1, "G1");
        when(materialGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(materialRepository.findByGroup(g)).thenReturn(List.of());
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Target MaterialGroup not found: 99");
    }

    @Test
    void deleteMaterialGroup_groupNotFound_throws() {
        when(materialGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMaterialGroup(99, DeleteGroupStrategy.CASCADE, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MaterialGroup not found: 99");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MaterialGroup group(int id, String name) {
        MaterialGroup g = new MaterialGroup();
        g.setId(id);
        g.setName(name);
        return g;
    }

    private Material material(int id, MaterialGroup group) {
        Material m = new Material();
        m.setId(id);
        m.setName("M" + id);
        m.setPrice(BigDecimal.ONE);
        m.setGroup(group);
        return m;
    }
}
