package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.CalculationItemDto;
import ru.denis.Calculator.Dto.Request.CalculationItemRequest;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Entity.Calculation;
import ru.denis.Calculator.Entity.CalculationItem;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Foundation.CalculationItemRepository;
import ru.denis.Calculator.Foundation.CalculationRepository;
import ru.denis.Calculator.Foundation.FormulaRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.FormulaEvaluator;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class CalculationServiceImpl implements ICalculationService {

    private final CalculationRepository calculationRepository;
    private final CalculationItemRepository calculationItemRepository;
    private final FormulaRepository formulaRepository;
    private final MaterialRepository materialRepository;
    private final FormulaEvaluator formulaEvaluator;

    public CalculationServiceImpl(CalculationRepository calculationRepository,
                                  CalculationItemRepository calculationItemRepository,
                                  FormulaRepository formulaRepository,
                                  MaterialRepository materialRepository,
                                  FormulaEvaluator formulaEvaluator) {
        this.calculationRepository = calculationRepository;
        this.calculationItemRepository = calculationItemRepository;
        this.formulaRepository = formulaRepository;
        this.materialRepository = materialRepository;
        this.formulaEvaluator = formulaEvaluator;
    }

    @Override
    public List<CalculationDto> getAllCalculations() {
        return calculationRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CalculationDto getCalculationById(Integer id) {
        return toDto(calculationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calculation not found: " + id)));
    }

    @Override
    @Transactional
    public CalculationDto createCalculation(CalculationRequest request) {
        Formula formula = formulaRepository.findById(request.formulaId())
                .orElseThrow(() -> new RuntimeException("Formula not found: " + request.formulaId()));

        Calculation calculation = new Calculation();
        calculation.setName(request.name());
        calculation.setFormula(formula);
        calculation = calculationRepository.save(calculation);

        List<CalculationItem> items = buildItems(calculation, request.items());
        calculationItemRepository.saveAll(items);
        calculation.setItems(items);

        return toDto(calculation);
    }

    @Override
    @Transactional
    public CalculationDto editCalculation(Integer id, CalculationRequest request) {
        Calculation calculation = calculationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calculation not found: " + id));
        Formula formula = formulaRepository.findById(request.formulaId())
                .orElseThrow(() -> new RuntimeException("Formula not found: " + request.formulaId()));

        calculationItemRepository.deleteAll(calculationItemRepository.findByCalculation(calculation));

        calculation.setName(request.name());
        calculation.setFormula(formula);

        List<CalculationItem> items = buildItems(calculation, request.items());
        calculationItemRepository.saveAll(items);
        calculation.setItems(items);

        return toDto(calculation);
    }

    @Override
    public void deleteCalculation(Integer id) {
        calculationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calculation not found: " + id));
        calculationRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------

    private List<CalculationItem> buildItems(Calculation calculation,
                                             List<CalculationItemRequest> requests) {
        return requests.stream().map(r -> {
            Material material = r.materialId() != null
                    ? materialRepository.findById(r.materialId()).orElse(null)
                    : null;
            CalculationItem item = new CalculationItem();
            item.setPosition(r.position());
            item.setQuantity(r.quantity());
            item.setCalculation(calculation);
            item.setMaterial(material);
            return item;
        }).toList();
    }

    private CalculationDto toDto(Calculation calculation) {
        String expression = calculation.getFormula().getExpression();
        List<String> placeholders = formulaEvaluator.extractPlaceholders(expression);

        List<CalculationItem> sortedItems = calculation.getItems() == null ? List.of()
                : calculation.getItems().stream()
                        .sorted(Comparator.comparingInt(i -> i.getPosition().intValue()))
                        .toList();

        List<CalculationItemDto> itemDtos = java.util.stream.IntStream.range(0, sortedItems.size())
                .mapToObj(i -> {
                    boolean isConst = i < placeholders.size()
                            && "const".equalsIgnoreCase(placeholders.get(i));
                    return itemToDto(sortedItems.get(i), isConst);
                })
                .toList();

        BigDecimal result = formulaEvaluator.evaluate(expression, sortedItems).orElse(null);

        return new CalculationDto(
                calculation.getId(),
                calculation.getName(),
                calculation.getFormula().getId(),
                calculation.getFormula().getName(),
                expression,
                calculation.getFormula().getGroup().getId(),
                calculation.getFormula().getGroup().getName(),
                itemDtos,
                result
        );
    }

    private CalculationItemDto itemToDto(CalculationItem item, boolean isConst) {
        return new CalculationItemDto(
                item.getId(),
                item.getPosition(),
                item.getQuantity(),
                item.getMaterial() != null ? item.getMaterial().getId() : null,
                item.getMaterial() != null ? item.getMaterial().getName() : null,
                item.getMaterial() != null ? item.getMaterial().getPrice() : null,
                item.getMaterial() != null ? item.getMaterial().getUnits() : null,
                isConst
        );
    }
}
