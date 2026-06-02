package ru.denis.Calculator.Mediator.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;

public interface IFormulaService {

    Page<FormulaDto> getAllFormulas(Integer groupId, Pageable pageable);

    FormulaDto getFormulaById(Integer id);

    FormulaDto createFormula(FormulaRequest request);

    FormulaDto editFormula(Integer id, FormulaRequest request);

    void deleteFormula(Integer id);

}
