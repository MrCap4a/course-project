package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationItemRequest;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Entity.*;
import ru.denis.Calculator.Foundation.CalculationItemRepository;
import ru.denis.Calculator.Foundation.CalculationRepository;
import ru.denis.Calculator.Foundation.FormulaRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.FormulaEvaluator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {

    @Mock private CalculationRepository calculationRepository;
    @Mock private CalculationItemRepository calculationItemRepository;
    @Mock private FormulaRepository formulaRepository;
    @Mock private MaterialRepository materialRepository;
    @Spy  private FormulaEvaluator formulaEvaluator = new FormulaEvaluator();

    @InjectMocks private CalculationServiceImpl service;

    // ── getAllCalculations ────────────────────────────────────────────────────

    @Test
    void getAllCalculations_returnsMappedList() {
        Calculation c = calculation(1, "C1", formula(1, "F1", "100"), List.of());
        when(calculationRepository.findAll()).thenReturn(List.of(c));

        List<CalculationDto> result = service.getAllCalculations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("C1");
    }

    @Test
    void getAllCalculations_empty_returnsEmptyList() {
        when(calculationRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllCalculations()).isEmpty();
    }

    // ── getCalculationById ───────────────────────────────────────────────────

    @Test
    void getCalculationById_found_returnsDto() {
        Calculation c = calculation(1, "C1", formula(1, "F1", "100"), List.of());
        when(calculationRepository.findById(1)).thenReturn(Optional.of(c));

        CalculationDto dto = service.getCalculationById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("C1");
        assertThat(dto.result()).isEqualByComparingTo("100");
    }

    @Test
    void getCalculationById_notFound_throws() {
        when(calculationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCalculationById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Calculation not found: 99");
    }

    // ── createCalculation ────────────────────────────────────────────────────

    @Test
    void createCalculation_noItems_savesAndReturnsDto() {
        Formula f = formula(1, "F1", "50");
        Calculation saved = calculation(5, "NewCalc", f, List.of());
        when(formulaRepository.findById(1)).thenReturn(Optional.of(f));
        when(calculationRepository.save(any())).thenReturn(saved);
        when(calculationItemRepository.saveAll(any())).thenReturn(List.of());

        CalculationRequest req = new CalculationRequest("NewCalc", 1, List.of());
        CalculationDto dto = service.createCalculation(req);

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.name()).isEqualTo("NewCalc");
        assertThat(dto.formulaId()).isEqualTo(1);
        assertThat(dto.result()).isEqualByComparingTo("50");
    }

    @Test
    void createCalculation_withConstItem_evaluatesCorrectly() {
        Formula f = formula(1, "F1", "{const} * 2");
        CalculationItemRequest itemReq = new CalculationItemRequest((short) 1, new BigDecimal("5"), null);
        CalculationItem savedItem = constItem(1, (short) 1, new BigDecimal("5"));

        Calculation saved = calculation(5, "C", f, List.of());
        when(formulaRepository.findById(1)).thenReturn(Optional.of(f));
        when(calculationRepository.save(any())).thenReturn(saved);
        when(calculationItemRepository.saveAll(any())).thenReturn(List.of(savedItem));

        CalculationRequest req = new CalculationRequest("C", 1, List.of(itemReq));
        CalculationDto dto = service.createCalculation(req);

        // items are set on calculation object directly; result = 5 * 2 = 10
        assertThat(dto.result()).isEqualByComparingTo("10");
    }

    @Test
    void createCalculation_formulaNotFound_throws() {
        when(formulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCalculation(new CalculationRequest("C", 99, List.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formula not found: 99");
    }

    // ── editCalculation ───────────────────────────────────────────────────────

    @Test
    void editCalculation_success_deletesOldItemsAndSavesNew() {
        Formula f = formula(1, "F1", "100");
        Calculation existing = calculation(1, "Old", f, List.of());
        Calculation saved = calculation(1, "New", f, List.of());
        when(calculationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(formulaRepository.findById(1)).thenReturn(Optional.of(f));
        when(calculationItemRepository.findByCalculation(existing)).thenReturn(List.of());
        when(calculationItemRepository.saveAll(any())).thenReturn(List.of());
        when(calculationRepository.save(existing)).thenReturn(saved);

        CalculationDto dto = service.editCalculation(1, new CalculationRequest("New", 1, List.of()));

        assertThat(dto.name()).isEqualTo("New");
        verify(calculationItemRepository).deleteAll(any());
    }

    @Test
    void editCalculation_calculationNotFound_throws() {
        when(calculationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editCalculation(99, new CalculationRequest("X", 1, List.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Calculation not found: 99");
    }

    @Test
    void editCalculation_formulaNotFound_throws() {
        Formula f = formula(1, "F1", "100");
        Calculation existing = calculation(1, "Old", f, List.of());
        when(calculationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(formulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editCalculation(1, new CalculationRequest("X", 99, List.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formula not found: 99");
    }

    // ── deleteCalculation ─────────────────────────────────────────────────────

    @Test
    void deleteCalculation_found_callsDeleteById() {
        Formula f = formula(1, "F1", "1");
        when(calculationRepository.findById(1)).thenReturn(Optional.of(calculation(1, "C", f, List.of())));

        service.deleteCalculation(1);

        verify(calculationRepository).deleteById(1);
    }

    @Test
    void deleteCalculation_notFound_throws() {
        when(calculationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCalculation(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Calculation not found: 99");
    }

    // ── DTO mapping — items ───────────────────────────────────────────────────

    @Test
    void getCalculationById_nullItems_returnsEmptyItemList() {
        Calculation c = calculation(1, "C", formula(1, "F", "0"), null);
        when(calculationRepository.findById(1)).thenReturn(Optional.of(c));

        CalculationDto dto = service.getCalculationById(1);

        assertThat(dto.items()).isEmpty();
    }

    @Test
    void getCalculationById_materialItemWithDeletedMaterial_resultIsNull() {
        // material placeholder but item has null material → evaluate returns empty
        Formula f = formula(1, "F", "{material}");
        CalculationItem item = constItem(1, (short) 1, new BigDecimal("5"));
        item.setMaterial(null);
        Calculation c = calculation(1, "C", f, List.of(item));
        when(calculationRepository.findById(1)).thenReturn(Optional.of(c));

        CalculationDto dto = service.getCalculationById(1);

        assertThat(dto.result()).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Formula formula(int id, String name, String expression) {
        FormulaGroup g = new FormulaGroup();
        g.setId(1);
        g.setName("G");
        Formula f = new Formula();
        f.setId(id);
        f.setName(name);
        f.setExpression(expression);
        f.setGroup(g);
        return f;
    }

    private Calculation calculation(int id, String name, Formula formula, List<CalculationItem> items) {
        Calculation c = new Calculation();
        c.setId(id);
        c.setName(name);
        c.setFormula(formula);
        c.setItems(items);
        return c;
    }

    private CalculationItem constItem(int id, short position, BigDecimal quantity) {
        CalculationItem item = new CalculationItem();
        item.setId(id);
        item.setPosition(position);
        item.setQuantity(quantity);
        item.setMaterial(null);
        return item;
    }
}
