package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.util.List;

@Service
public class CalculationServiceProxy implements ICalculationService {

    private final ICalculationService delegate;
    private final PermissionChecker checker;

    public CalculationServiceProxy(
            @Qualifier("calculationServiceImpl") ICalculationService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    @Override
    public List<CalculationDto> getAllCalculations() {
        checker.require("calculations.view");
        return delegate.getAllCalculations();
    }

    @Override
    public CalculationDto getCalculationById(Integer id) {
        checker.require("calculations.view");
        return delegate.getCalculationById(id);
    }

    @Override
    public CalculationDto createCalculation(CalculationRequest request) {
        checker.require("calculations.create");
        return delegate.createCalculation(request);
    }

    @Override
    public CalculationDto editCalculation(Integer id, CalculationRequest request) {
        checker.require("calculations.edit");
        return delegate.editCalculation(id, request);
    }

    @Override
    public void deleteCalculation(Integer id) {
        checker.require("calculations.delete");
        delegate.deleteCalculation(id);
    }
}
