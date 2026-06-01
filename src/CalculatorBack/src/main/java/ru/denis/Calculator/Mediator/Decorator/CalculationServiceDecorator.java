package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.util.List;

@Slf4j
@Service
@Primary
public class CalculationServiceDecorator implements ICalculationService {

    private final ICalculationService delegate;

    public CalculationServiceDecorator(
            @Qualifier("calculationServiceProxy") ICalculationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<CalculationDto> getAllCalculations() {
        log.info("getAllCalculations user={}", currentUser());
        try {
            List<CalculationDto> result = delegate.getAllCalculations();
            log.info("getAllCalculations: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllCalculations failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public CalculationDto getCalculationById(Integer id) {
        log.info("getCalculationById id={} user={}", id, currentUser());
        try {
            return delegate.getCalculationById(id);
        } catch (Exception e) {
            log.error("getCalculationById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public CalculationDto createCalculation(CalculationRequest request) {
        log.info("createCalculation name={} formulaId={} user={}", request.name(), request.formulaId(), currentUser());
        try {
            CalculationDto result = delegate.createCalculation(request);
            log.info("createCalculation created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createCalculation failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public CalculationDto editCalculation(Integer id, CalculationRequest request) {
        log.info("editCalculation id={} user={}", id, currentUser());
        try {
            CalculationDto result = delegate.editCalculation(id, request);
            log.info("editCalculation completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editCalculation failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCalculation(Integer id) {
        log.info("deleteCalculation id={} user={}", id, currentUser());
        try {
            delegate.deleteCalculation(id);
            log.info("deleteCalculation completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteCalculation failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
