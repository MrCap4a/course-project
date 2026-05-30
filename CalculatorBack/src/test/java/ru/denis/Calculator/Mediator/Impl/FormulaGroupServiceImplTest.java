package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaGroupServiceImplTest {

    @Mock private FormulaGroupRepository formulaGroupRepository;
    @InjectMocks private FormulaGroupServiceImpl service;

    // ── getAllFormulaGroups ───────────────────────────────────────────────────

    @Test
    void getAllFormulaGroups_returnsMappedList() {
        when(formulaGroupRepository.findAll()).thenReturn(List.of(group(1, "A"), group(2, "B")));

        List<FormulaGroupDto> result = service.getAllFormulaGroups();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FormulaGroupDto::name).containsExactly("A", "B");
    }

    @Test
    void getAllFormulaGroups_empty_returnsEmptyList() {
        when(formulaGroupRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllFormulaGroups()).isEmpty();
    }

    // ── getFormulaGroupById ──────────────────────────────────────────────────

    @Test
    void getFormulaGroupById_found_returnsDto() {
        when(formulaGroupRepository.findById(1)).thenReturn(Optional.of(group(1, "G1")));

        FormulaGroupDto dto = service.getFormulaGroupById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("G1");
    }

    @Test
    void getFormulaGroupById_notFound_throws() {
        when(formulaGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFormulaGroupById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FormulaGroup not found: 99");
    }

    // ── createFormulaGroup ───────────────────────────────────────────────────

    @Test
    void createFormulaGroup_savesAndReturnsDto() {
        when(formulaGroupRepository.save(any())).thenReturn(group(5, "NewGroup"));

        FormulaGroupDto dto = service.createFormulaGroup(new FormulaGroupRequest("NewGroup"));

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.name()).isEqualTo("NewGroup");
        verify(formulaGroupRepository).save(any());
    }

    // ── editFormulaGroup ─────────────────────────────────────────────────────

    @Test
    void editFormulaGroup_found_updatesNameAndReturns() {
        FormulaGroup g = group(1, "Old");
        when(formulaGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(formulaGroupRepository.save(g)).thenReturn(group(1, "New"));

        FormulaGroupDto dto = service.editFormulaGroup(1, new FormulaGroupRequest("New"));

        assertThat(dto.name()).isEqualTo("New");
    }

    @Test
    void editFormulaGroup_notFound_throws() {
        when(formulaGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editFormulaGroup(99, new FormulaGroupRequest("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FormulaGroup not found: 99");
    }

    // ── deleteFormulaGroup ───────────────────────────────────────────────────

    @Test
    void deleteFormulaGroup_found_callsDeleteById() {
        when(formulaGroupRepository.findById(1)).thenReturn(Optional.of(group(1, "G1")));

        service.deleteFormulaGroup(1);

        verify(formulaGroupRepository).deleteById(1);
    }

    @Test
    void deleteFormulaGroup_notFound_throws() {
        when(formulaGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFormulaGroup(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FormulaGroup not found: 99");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private FormulaGroup group(int id, String name) {
        FormulaGroup g = new FormulaGroup();
        g.setId(id);
        g.setName(name);
        return g;
    }
}
