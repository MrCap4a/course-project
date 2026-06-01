package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

@Service
public class FormulaServiceProxy implements IFormulaService {

    private final IFormulaService delegate;
    private final PermissionChecker checker;

    public FormulaServiceProxy(
            @Qualifier("formulaServiceImpl") IFormulaService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    @Override
    public Page<FormulaDto> getAllFormulas(Integer groupId, Pageable pageable) {
        checker.require("formulas.view");
        return delegate.getAllFormulas(groupId, pageable);
    }

    @Override
    public FormulaDto getFormulaById(Integer id) {
        checker.require("formulas.view");
        return delegate.getFormulaById(id);
    }

    @Override
    public FormulaDto createFormula(FormulaRequest request) {
        checker.require("formulas.create");
        return delegate.createFormula(request);
    }

    @Override
    public FormulaDto editFormula(Integer id, FormulaRequest request) {
        checker.require("formulas.edit");
        return delegate.editFormula(id, request);
    }

    @Override
    public void deleteFormula(Integer id) {
        checker.require("formulas.delete");
        delegate.deleteFormula(id);
    }
}
