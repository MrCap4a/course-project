package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationRequest;

import java.util.List;

public interface ICalculationService {
    List<CalculationDto> getAllCalculations();
    CalculationDto getCalculationById(Integer id);
    CalculationDto createCalculation(CalculationRequest request);
    CalculationDto editCalculation(Integer id, CalculationRequest request);
    void deleteCalculation(Integer id);
}
