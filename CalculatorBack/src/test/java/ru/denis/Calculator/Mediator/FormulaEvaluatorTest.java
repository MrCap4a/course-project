package ru.denis.Calculator.Mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.denis.Calculator.Entity.CalculationItem;
import ru.denis.Calculator.Entity.Material;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FormulaEvaluatorTest {

    private FormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FormulaEvaluator();
    }

    // ── extractPlaceholders ──────────────────────────────────────────────────

    @Test
    void extractPlaceholders_noPlaceholders_returnsEmpty() {
        assertThat(evaluator.extractPlaceholders("2 + 3")).isEmpty();
    }

    @Test
    void extractPlaceholders_singleConst_returnsConst() {
        assertThat(evaluator.extractPlaceholders("{const}")).containsExactly("const");
    }

    @Test
    void extractPlaceholders_singleMaterial_returnsMaterial() {
        assertThat(evaluator.extractPlaceholders("{material}")).containsExactly("material");
    }

    @Test
    void extractPlaceholders_multipleMixed_returnsInOrder() {
        List<String> result = evaluator.extractPlaceholders("{const} + {material} * {const}");
        assertThat(result).containsExactly("const", "material", "const");
    }

    // ── evaluate — pure arithmetic (no placeholders) ─────────────────────────

    @Test
    void evaluate_addition_returnsSum() {
        assertThat(eval("2 + 3")).isEqualByComparingTo("5");
    }

    @Test
    void evaluate_subtraction_returnsDifference() {
        assertThat(eval("10 - 4")).isEqualByComparingTo("6");
    }

    @Test
    void evaluate_multiplication_returnsProduct() {
        assertThat(eval("4 * 5")).isEqualByComparingTo("20");
    }

    @Test
    void evaluate_division_returnsQuotient() {
        assertThat(eval("10 / 4")).isEqualByComparingTo("2.5");
    }

    @Test
    void evaluate_parentheses_overridesPrecedence() {
        assertThat(eval("(2 + 3) * 4")).isEqualByComparingTo("20");
    }

    @Test
    void evaluate_operatorPrecedence_multiplyBeforeAdd() {
        assertThat(eval("2 + 3 * 4")).isEqualByComparingTo("14");
    }

    @Test
    void evaluate_negativeNumberInExpression_correctResult() {
        assertThat(eval("5 + -3")).isEqualByComparingTo("2");
    }

    // ── evaluate — const placeholder ────────────────────────────────────────

    @Test
    void evaluate_constPlaceholder_substitutesQuantity() {
        CalculationItem item = constItem((short) 1, new BigDecimal("7"));
        Optional<BigDecimal> result = evaluator.evaluate("{const} * 2", List.of(item));
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("14");
    }

    @Test
    void evaluate_twoConstPlaceholders_usesItemsInOrder() {
        CalculationItem c1 = constItem((short) 1, new BigDecimal("10"));
        CalculationItem c2 = constItem((short) 2, new BigDecimal("2"));
        // 10 + 2 * 3 = 16
        Optional<BigDecimal> result = evaluator.evaluate("{const} + {const} * 3", List.of(c1, c2));
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("16");
    }

    // ── evaluate — material placeholder ─────────────────────────────────────

    @Test
    void evaluate_materialPlaceholder_substitutesPriceTimesQuantity() {
        CalculationItem item = materialItem((short) 1, new BigDecimal("3"), new BigDecimal("5"));
        Optional<BigDecimal> result = evaluator.evaluate("{material}", List.of(item));
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("15");
    }

    @Test
    void evaluate_materialPlaceholderNullMaterial_returnsEmpty() {
        CalculationItem item = new CalculationItem();
        item.setPosition((short) 1);
        item.setQuantity(new BigDecimal("3"));
        item.setMaterial(null);
        assertThat(evaluator.evaluate("{material}", List.of(item))).isEmpty();
    }

    // ── evaluate — edge / error cases ────────────────────────────────────────

    @Test
    void evaluate_fewerItemsThanPlaceholders_returnsEmpty() {
        assertThat(evaluator.evaluate("{const} + {const}", List.of())).isEmpty();
    }

    @Test
    void evaluate_invalidExpression_returnsEmpty() {
        assertThat(evaluator.evaluate("abc", List.of())).isEmpty();
    }

    @Test
    void evaluate_emptyItemsAndNoPlaceholders_returnsValue() {
        Optional<BigDecimal> result = evaluator.evaluate("100", List.of());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("100");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private BigDecimal eval(String expression) {
        return evaluator.evaluate(expression, List.of())
                .orElseThrow(() -> new AssertionError("evaluate returned empty for: " + expression));
    }

    private CalculationItem constItem(short pos, BigDecimal qty) {
        CalculationItem item = new CalculationItem();
        item.setPosition(pos);
        item.setQuantity(qty);
        return item;
    }

    private CalculationItem materialItem(short pos, BigDecimal qty, BigDecimal price) {
        Material mat = new Material();
        mat.setPrice(price);
        CalculationItem item = new CalculationItem();
        item.setPosition(pos);
        item.setQuantity(qty);
        item.setMaterial(mat);
        return item;
    }
}
