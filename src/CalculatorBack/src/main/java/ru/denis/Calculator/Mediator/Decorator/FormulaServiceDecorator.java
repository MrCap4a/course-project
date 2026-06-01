package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

import java.util.List;

@Slf4j
@Service
@Primary
public class FormulaServiceDecorator implements IFormulaService {

    private final IFormulaService delegate;

    public FormulaServiceDecorator(
            @Qualifier("formulaServiceProxy") IFormulaService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<FormulaDto> getAllFormulas() {
        log.info("getAllFormulas user={}", currentUser());
        try {
            List<FormulaDto> result = delegate.getAllFormulas();
            log.info("getAllFormulas: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllFormulas failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaDto getFormulaById(Integer id) {
        log.info("getFormulaById id={} user={}", id, currentUser());
        try {
            return delegate.getFormulaById(id);
        } catch (Exception e) {
            log.error("getFormulaById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaDto createFormula(FormulaRequest request) {
        log.info("createFormula name={} user={}", request.name(), currentUser());
        try {
            FormulaDto result = delegate.createFormula(request);
            log.info("createFormula created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createFormula failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaDto editFormula(Integer id, FormulaRequest request) {
        log.info("editFormula id={} user={}", id, currentUser());
        try {
            FormulaDto result = delegate.editFormula(id, request);
            log.info("editFormula completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editFormula failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteFormula(Integer id) {
        log.info("deleteFormula id={} user={}", id, currentUser());
        try {
            delegate.deleteFormula(id);
            log.info("deleteFormula completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteFormula failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
