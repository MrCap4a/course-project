package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;

import java.util.List;

@Slf4j
@Service
@Primary
public class FormulaGroupServiceDecorator implements IFormulaGroupService {

    private final IFormulaGroupService delegate;

    public FormulaGroupServiceDecorator(
            @Qualifier("formulaGroupServiceProxy") IFormulaGroupService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<FormulaGroupDto> getAllFormulaGroups() {
        log.info("getAllFormulaGroups user={}", currentUser());
        try {
            List<FormulaGroupDto> result = delegate.getAllFormulaGroups();
            log.info("getAllFormulaGroups: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllFormulaGroups failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaGroupDto getFormulaGroupById(Integer id) {
        log.info("getFormulaGroupById id={} user={}", id, currentUser());
        try {
            return delegate.getFormulaGroupById(id);
        } catch (Exception e) {
            log.error("getFormulaGroupById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaGroupDto createFormulaGroup(FormulaGroupRequest request) {
        log.info("createFormulaGroup name={} user={}", request.name(), currentUser());
        try {
            FormulaGroupDto result = delegate.createFormulaGroup(request);
            log.info("createFormulaGroup created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createFormulaGroup failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FormulaGroupDto editFormulaGroup(Integer id, FormulaGroupRequest request) {
        log.info("editFormulaGroup id={} user={}", id, currentUser());
        try {
            FormulaGroupDto result = delegate.editFormulaGroup(id, request);
            log.info("editFormulaGroup completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editFormulaGroup failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteFormulaGroup(Integer id) {
        log.info("deleteFormulaGroup id={} user={}", id, currentUser());
        try {
            delegate.deleteFormulaGroup(id);
            log.info("deleteFormulaGroup completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteFormulaGroup failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
