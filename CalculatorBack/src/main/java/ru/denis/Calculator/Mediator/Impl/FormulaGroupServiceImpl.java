package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Entity.FormulaGroup;
import ru.denis.Calculator.Foundation.FormulaGroupRepository;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;

import java.util.List;

@Service
public class FormulaGroupServiceImpl implements IFormulaGroupService {

    private final FormulaGroupRepository formulaGroupRepository;

    public FormulaGroupServiceImpl(FormulaGroupRepository formulaGroupRepository) {
        this.formulaGroupRepository = formulaGroupRepository;
    }

    @Override
    public List<FormulaGroupDto> getAllFormulaGroups() {
        return formulaGroupRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public FormulaGroupDto getFormulaGroupById(Integer id) {
        return toDto(formulaGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + id)));
    }

    @Override
    public FormulaGroupDto createFormulaGroup(FormulaGroupRequest request) {
        FormulaGroup group = new FormulaGroup();
        group.setName(request.name());
        return toDto(formulaGroupRepository.save(group));
    }

    @Override
    public FormulaGroupDto editFormulaGroup(Integer id, FormulaGroupRequest request) {
        FormulaGroup group = formulaGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + id));
        group.setName(request.name());
        return toDto(formulaGroupRepository.save(group));
    }

    @Override
    public void deleteFormulaGroup(Integer id) {
        formulaGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FormulaGroup not found: " + id));
        formulaGroupRepository.deleteById(id);
    }

    private FormulaGroupDto toDto(FormulaGroup group) {
        return new FormulaGroupDto(group.getId(), group.getName());
    }
}
