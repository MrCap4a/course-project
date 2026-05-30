package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;
import ru.denis.Calculator.Foundation.FormulaRepository;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

import java.util.List;

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
    public List<FormulaDto> getAllFormulas() {
        return formulaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public FormulaDto getFormulaById(Integer id) {
        return toDto(formulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formula not found: " + id)));
    }

    @Override
    public FormulaDto createFormula(FormulaRequest request) {
        FormulaGroup group = formulaGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + request.groupId()));

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
        FormulaGroup group = formulaGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + request.groupId()));

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
