package ru.denis.Calculator.Mediator.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Config.DataInitializer;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;
import ru.denis.Calculator.Foundation.FormulaRepository;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

@Service
public class FormulaServiceImpl implements IFormulaService {

    private final FormulaRepository formulaRepository;
    private final FormulaGroupRepository formulaGroupRepository;

    public FormulaServiceImpl(FormulaRepository formulaRepository,
                              FormulaGroupRepository formulaGroupRepository) {
        this.formulaRepository = formulaRepository;
        this.formulaGroupRepository = formulaGroupRepository;
    }

    @Override
    public Page<FormulaDto> getAllFormulas(Integer groupId, Pageable pageable) {
        if (groupId != null) {
            FormulaGroup group = formulaGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + groupId));
            return formulaRepository.findByGroup(group, pageable).map(this::toDto);
        }
        return formulaRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public FormulaDto getFormulaById(Integer id) {
        return toDto(formulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formula not found: " + id)));
    }

    @Override
    public FormulaDto createFormula(FormulaRequest request) {
        FormulaGroup group = resolveGroup(request.groupId());
        Formula formula = new Formula();
        formula.setName(request.name());
        formula.setExpression(request.expression());
        formula.setGroup(group);
        return toDto(formulaRepository.save(formula));
    }

    @Override
    public FormulaDto editFormula(Integer id, FormulaRequest request) {
        Formula formula = formulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formula not found: " + id));
        FormulaGroup group = resolveGroup(request.groupId());
        formula.setName(request.name());
        formula.setExpression(request.expression());
        formula.setGroup(group);
        return toDto(formulaRepository.save(formula));
    }

    @Override
    public void deleteFormula(Integer id) {
        formulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formula not found: " + id));
        formulaRepository.deleteById(id);
    }

    private FormulaGroup resolveGroup(Integer groupId) {
        if (groupId != null) {
            return formulaGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + groupId));
        }
        return formulaGroupRepository.findByNameIgnoreCase(DataInitializer.DEFAULT_GROUP_NAME)
                .orElseGet(() -> {
                    FormulaGroup def = new FormulaGroup();
                    def.setName(DataInitializer.DEFAULT_GROUP_NAME);
                    return formulaGroupRepository.save(def);
                });
    }

    private FormulaDto toDto(Formula formula) {
        return new FormulaDto(
                formula.getId(),
                formula.getName(),
                formula.getExpression(),
                formula.getGroup().getId(),
                formula.getGroup().getName()
        );
    }
}
