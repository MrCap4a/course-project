package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;

import java.util.List;

@Service
public class FormulaGroupServiceProxy implements IFormulaGroupService {

    private final IFormulaGroupService delegate;
    private final PermissionChecker checker;

    public FormulaGroupServiceProxy(
            @Qualifier("formulaGroupServiceImpl") IFormulaGroupService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    @Override
    public List<FormulaGroupDto> getAllFormulaGroups() {
        checker.require("formulas.view");
        return delegate.getAllFormulaGroups();
    }

    @Override
    public FormulaGroupDto getFormulaGroupById(Integer id) {
        checker.require("formulas.view");
        return delegate.getFormulaGroupById(id);
    }

    @Override
    public FormulaGroupDto createFormulaGroup(FormulaGroupRequest request) {
        checker.require("formulas.create");
        return delegate.createFormulaGroup(request);
    }

    @Override
    public FormulaGroupDto editFormulaGroup(Integer id, FormulaGroupRequest request) {
        checker.require("formulas.edit");
        return delegate.editFormulaGroup(id, request);
    }

    @Override
    public void deleteFormulaGroup(Integer id) {
        checker.require("formulas.delete");
        delegate.deleteFormulaGroup(id);
    }
}
