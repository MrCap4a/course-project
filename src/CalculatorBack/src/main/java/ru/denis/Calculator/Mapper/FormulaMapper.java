package ru.denis.Calculator.Mapper;

import org.springframework.stereotype.Component;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;

/**
 * Data Mapper для сущности {@link Formula}.
 *
 * <p>Применяет тот же принцип, что и {@link MaterialMapper}: вся логика
 * преобразования Entity ↔ DTO сосредоточена в одном месте.
 * {@code FormulaServiceImpl} больше не содержит встроенных конструкций
 * {@code new FormulaDto(...)}, а делегирует их этому классу.</p>
 */
@Component
public class FormulaMapper {

    /**
     * Преобразует JPA-сущность {@link Formula} в DTO.
     *
     * @param formula сущность из БД (не null)
     * @return DTO с плоской структурой данных
     */
    public FormulaDto toDto(Formula formula) {
        return new FormulaDto(
                formula.getId(),
                formula.getName(),
                formula.getExpression(),
                formula.getGroup().getId(),
                formula.getGroup().getName()
        );
    }

    /**
     * Создаёт новую {@link Formula} из запроса без сохранения.
     *
     * @param request данные из тела HTTP-запроса
     * @param group   уже загруженная или разрешённая группа
     * @return несохранённая сущность
     */
    public Formula toEntity(FormulaRequest request, FormulaGroup group) {
        Formula formula = new Formula();
        formula.setName(request.name());
        formula.setExpression(request.expression());
        formula.setGroup(group);
        return formula;
    }

    /**
     * Применяет данные из запроса к существующей сущности (update-сценарий).
     *
     * @param formula сущность из БД
     * @param request данные из тела HTTP-запроса
     * @param group   уже загруженная или разрешённая группа
     */
    public void applyRequest(Formula formula, FormulaRequest request, FormulaGroup group) {
        formula.setName(request.name());
        formula.setExpression(request.expression());
        formula.setGroup(group);
    }
}
