package ru.denis.Calculator.Control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.util.List;

@RestController
@RequestMapping("/calculations")
public class CalculationController {

    private final ICalculationService calculationService;

    public CalculationController(ICalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping
    public ResponseEntity<List<CalculationDto>> getAllCalculations() {
        return ResponseEntity.ok(calculationService.getAllCalculations());
    }

    @PostMapping
    public ResponseEntity<CalculationDto> createCalculation(@RequestBody CalculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calculationService.createCalculation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalculationDto> getCalculationById(@PathVariable Integer id) {
        return ResponseEntity.ok(calculationService.getCalculationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalculationDto> editCalculation(@PathVariable Integer id,
                                                          @RequestBody CalculationRequest request) {
        return ResponseEntity.ok(calculationService.editCalculation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalculation(@PathVariable Integer id) {
        calculationService.deleteCalculation(id);
        return ResponseEntity.noContent().build();
    }
}
