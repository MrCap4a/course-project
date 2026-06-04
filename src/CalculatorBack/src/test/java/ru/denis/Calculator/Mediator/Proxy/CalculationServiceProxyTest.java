package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.CalculationDto;
import ru.denis.Calculator.Dto.Request.CalculationRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.ICalculationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationServiceProxyTest {

    @Mock private ICalculationService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private CalculationServiceProxy proxy;

    private CalculationDto dto() {
        return new CalculationDto(1, "Roof", 1, "Area", "{const}", 1, "G", List.of(), BigDecimal.ONE);
    }

    @Test
    void getAllCalculations_requiresViewPermission() {
        when(delegate.getAllCalculations()).thenReturn(List.of());
        proxy.getAllCalculations();
        verify(checker).require("calculations.view");
        verify(delegate).getAllCalculations();
    }

    @Test
    void getAllCalculations_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("calculations.view");
        assertThatThrownBy(() -> proxy.getAllCalculations()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCalculationById_requiresViewPermission() {
        when(delegate.getCalculationById(1)).thenReturn(dto());
        proxy.getCalculationById(1);
        verify(checker).require("calculations.view");
<<<<<<< Updated upstream
        verify(delegate).getCalculationById(1);
=======
>>>>>>> Stashed changes
    }

    @Test
    void createCalculation_requiresCreatePermission() {
        CalculationRequest req = new CalculationRequest("Roof", 1, List.of());
        when(delegate.createCalculation(req)).thenReturn(dto());
        proxy.createCalculation(req);
        verify(checker).require("calculations.create");
<<<<<<< Updated upstream
        verify(delegate).createCalculation(req);
=======
>>>>>>> Stashed changes
    }

    @Test
    void editCalculation_requiresEditPermission() {
        CalculationRequest req = new CalculationRequest("Roof", 1, List.of());
        when(delegate.editCalculation(1, req)).thenReturn(dto());
        proxy.editCalculation(1, req);
        verify(checker).require("calculations.edit");
<<<<<<< Updated upstream
        verify(delegate).editCalculation(1, req);
=======
>>>>>>> Stashed changes
    }

    @Test
    void deleteCalculation_requiresDeletePermission() {
        proxy.deleteCalculation(1);
        verify(checker).require("calculations.delete");
        verify(delegate).deleteCalculation(1);
    }
}
