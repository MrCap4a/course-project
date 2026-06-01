package ru.denis.Calculator.Mediator;

import org.springframework.stereotype.Component;
import ru.denis.Calculator.Entity.CalculationItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FormulaEvaluator {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");

    /**
     * Вычисляет результат формулы, подставляя значения из позиций расчёта.
     * Позиции должны быть отсортированы по полю position.
     * Возвращает empty, если хотя бы одна материальная позиция имеет удалённый материал.
     */
    public Optional<BigDecimal> evaluate(String expression, List<CalculationItem> sortedItems) {
        StringBuffer substituted = new StringBuffer();
        Matcher matcher = PLACEHOLDER.matcher(expression);
        int index = 0;

        while (matcher.find()) {
            if (index >= sortedItems.size()) return Optional.empty();

            CalculationItem item = sortedItems.get(index++);
            String placeholder = matcher.group(1);

            BigDecimal value;
            if ("const".equalsIgnoreCase(placeholder)) {
                value = item.getQuantity();
            } else {
                if (item.getMaterial() == null) return Optional.empty();
                value = item.getMaterial().getPrice().multiply(item.getQuantity());
            }

            matcher.appendReplacement(substituted, value.toPlainString());
        }
        matcher.appendTail(substituted);

        try {
            return Optional.of(new Parser(substituted.toString()).parse());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Возвращает плейсхолдеры из выражения в порядке появления. */
    public List<String> extractPlaceholders(String expression) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(expression);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Рекурсивный нисходящий парсер арифметических выражений
    // Грамматика:
    //   expr   := term   (('+' | '-') term)*
    //   term   := factor (('*' | '/') factor)*
    //   factor := '(' expr ')' | number
    //   number := '-'? [0-9]+ ('.' [0-9]+)?
    // -------------------------------------------------------------------------

    private static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }

        BigDecimal parse() {
            BigDecimal result = parseExpr();
            if (pos != input.length()) {
                throw new IllegalArgumentException("Unexpected character at " + pos + ": " + input.charAt(pos));
            }
            return result;
        }

        private BigDecimal parseExpr() {
            BigDecimal result = parseTerm();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '+') { pos++; result = result.add(parseTerm()); }
                else if (op == '-') { pos++; result = result.subtract(parseTerm()); }
                else break;
            }
            return result;
        }

        private BigDecimal parseTerm() {
            BigDecimal result = parseFactor();
            while (pos < input.length()) {
                char op = input.charAt(pos);
                if (op == '*') {
                    pos++;
                    result = result.multiply(parseFactor());
                } else if (op == '/') {
                    pos++;
                    result = result.divide(parseFactor(), 10, RoundingMode.HALF_UP);
                } else break;
            }
            return result;
        }

        private BigDecimal parseFactor() {
            if (pos < input.length() && input.charAt(pos) == '(') {
                pos++;
                BigDecimal result = parseExpr();
                if (pos < input.length() && input.charAt(pos) == ')') pos++;
                return result;
            }
            return parseNumber();
        }

        private BigDecimal parseNumber() {
            int start = pos;
            if (pos < input.length() && input.charAt(pos) == '-') pos++;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            return new BigDecimal(input.substring(start, pos));
        }
    }
}
