package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;

import java.util.List;

public interface IFormulaService {
    List<FormulaDto> getAllFormulas();
    FormulaDto getFormulaById(Integer id);
    FormulaDto createFormula(FormulaRequest request);
    FormulaDto editFormula(Integer id, FormulaRequest request);
    void deleteFormula(Integer id);
}
