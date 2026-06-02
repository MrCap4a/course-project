package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;

import java.util.List;

public interface IFormulaGroupService {

    List<FormulaGroupDto> getAllFormulaGroups();

    FormulaGroupDto getFormulaGroupById(Integer id);

    FormulaGroupDto createFormulaGroup(FormulaGroupRequest request);

    FormulaGroupDto editFormulaGroup(Integer id, FormulaGroupRequest request);

    void deleteFormulaGroup(Integer id);

}
