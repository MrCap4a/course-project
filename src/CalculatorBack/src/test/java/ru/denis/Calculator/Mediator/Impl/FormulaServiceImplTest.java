package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;
import ru.denis.Calculator.Foundation.FormulaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaServiceImplTest {

    @Mock private FormulaRepository formulaRepository;
    @Mock private FormulaGroupRepository formulaGroupRepository;
    @InjectMocks private FormulaServiceImpl service;

    // ── getAllFormulas ────────────────────────────────────────────────────────

    @Test
    void getAllFormulas_returnsMappedList() {
        FormulaGroup g = group(1, "G1");
        when(formulaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(formula(1, "F1", "{const}*2", g))));

        Page<FormulaDto> result = service.getAllFormulas(null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("F1");
        assertThat(result.getContent().get(0).expression()).isEqualTo("{const}*2");
    }

    @Test
    void getAllFormulas_emptyRepository_returnsEmptyList() {
        when(formulaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertThat(service.getAllFormulas(null, Pageable.unpaged()).getContent()).isEmpty();
    }

    // ── getFormulaById ───────────────────────────────────────────────────────

    @Test
    void getFormulaById_found_returnsDto() {
        FormulaGroup g = group(1, "G1");
        when(formulaRepository.findById(1)).thenReturn(Optional.of(formula(1, "F1", "{const}", g)));

        FormulaDto dto = service.getFormulaById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("F1");
        assertThat(dto.groupId()).isEqualTo(1);
        assertThat(dto.groupName()).isEqualTo("G1");
    }

    @Test
    void getFormulaById_notFound_throws() {
        when(formulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFormulaById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formula not found: 99");
    }

    // ── createFormula ─────────────────────────────────────────────────────────

    @Test
    void createFormula_success_savesAndReturnsDto() {
        FormulaGroup g = group(1, "G1");
        Formula saved = formula(5, "NewFormula", "{const}+1", g);
        when(formulaGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(formulaRepository.save(any())).thenReturn(saved);

        FormulaDto dto = service.createFormula(new FormulaRequest("NewFormula", "{const}+1", 1));

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.name()).isEqualTo("NewFormula");
        assertThat(dto.expression()).isEqualTo("{const}+1");
        verify(formulaRepository).save(any());
    }

    @Test
    void createFormula_groupNotFound_throws() {
        when(formulaGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFormula(new FormulaRequest("F", "{const}", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FormulaGroup not found: 99");
    }

    // ── editFormula ───────────────────────────────────────────────────────────

    @Test
    void editFormula_success_updatesAndReturnsDto() {
        FormulaGroup g = group(1, "G1");
        Formula existing = formula(1, "Old", "1+1", g);
        Formula updated = formula(1, "New", "{const}*3", g);
        when(formulaRepository.findById(1)).thenReturn(Optional.of(existing));
        when(formulaGroupRepository.findById(1)).thenReturn(Optional.of(g));
        when(formulaRepository.save(existing)).thenReturn(updated);

        FormulaDto dto = service.editFormula(1, new FormulaRequest("New", "{const}*3", 1));

        assertThat(dto.name()).isEqualTo("New");
        assertThat(dto.expression()).isEqualTo("{const}*3");
    }

    @Test
    void editFormula_formulaNotFound_throws() {
        when(formulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editFormula(99, new FormulaRequest("X", "1", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formula not found: 99");
    }

    @Test
    void editFormula_groupNotFound_throws() {
        FormulaGroup g = group(1, "G1");
        when(formulaRepository.findById(1)).thenReturn(Optional.of(formula(1, "F", "1", g)));
        when(formulaGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editFormula(1, new FormulaRequest("F", "1", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FormulaGroup not found: 99");
    }

    // ── deleteFormula ─────────────────────────────────────────────────────────

    @Test
    void deleteFormula_found_callsDeleteById() {
        FormulaGroup g = group(1, "G1");
        when(formulaRepository.findById(1)).thenReturn(Optional.of(formula(1, "F", "1", g)));

        service.deleteFormula(1);

        verify(formulaRepository).deleteById(1);
    }

    @Test
    void deleteFormula_notFound_throws() {
        when(formulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFormula(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formula not found: 99");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private FormulaGroup group(int id, String name) {
        FormulaGroup g = new FormulaGroup();
        g.setId(id);
        g.setName(name);
        return g;
    }

    private Formula formula(int id, String name, String expression, FormulaGroup group) {
        Formula f = new Formula();
        f.setId(id);
        f.setName(name);
        f.setExpression(expression);
        f.setGroup(group);
        return f;
    }
}
