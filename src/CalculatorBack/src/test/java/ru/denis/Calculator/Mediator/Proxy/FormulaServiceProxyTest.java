package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaServiceProxyTest {

    @Mock private IFormulaService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private FormulaServiceProxy proxy;

<<<<<<< Updated upstream
    private FormulaDto dto() {
        return new FormulaDto(1, "Area", "{const}*2", 1, "Geometry");
    }

=======
>>>>>>> Stashed changes
    @Test
    @SuppressWarnings("unchecked")
    void getAllFormulas_requiresViewPermission() {
        Page<FormulaDto> page = mock(Page.class);
        when(delegate.getAllFormulas(null, Pageable.unpaged())).thenReturn(page);
        proxy.getAllFormulas(null, Pageable.unpaged());
        verify(checker).require("formulas.view");
<<<<<<< Updated upstream
        verify(delegate).getAllFormulas(null, Pageable.unpaged());
=======
>>>>>>> Stashed changes
    }

    @Test
    void getAllFormulas_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("formulas.view");
        assertThatThrownBy(() -> proxy.getAllFormulas(null, Pageable.unpaged()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getFormulaById_requiresViewPermission() {
<<<<<<< Updated upstream
        when(delegate.getFormulaById(1)).thenReturn(dto());
        proxy.getFormulaById(1);
        verify(checker).require("formulas.view");
        verify(delegate).getFormulaById(1);
=======
        when(delegate.getFormulaById(1)).thenReturn(new FormulaDto(1, "Area", "{const}*2", 1, "G"));
        proxy.getFormulaById(1);
        verify(checker).require("formulas.view");
>>>>>>> Stashed changes
    }

    @Test
    void createFormula_requiresCreatePermission() {
        FormulaRequest req = new FormulaRequest("Area", "{const}*2", 1);
<<<<<<< Updated upstream
        when(delegate.createFormula(req)).thenReturn(dto());
        proxy.createFormula(req);
        verify(checker).require("formulas.create");
        verify(delegate).createFormula(req);
=======
        when(delegate.createFormula(req)).thenReturn(new FormulaDto(1, "Area", "{const}*2", 1, "G"));
        proxy.createFormula(req);
        verify(checker).require("formulas.create");
>>>>>>> Stashed changes
    }

    @Test
    void editFormula_requiresEditPermission() {
        FormulaRequest req = new FormulaRequest("Area2", "{const}*3", 1);
<<<<<<< Updated upstream
        when(delegate.editFormula(1, req)).thenReturn(dto());
        proxy.editFormula(1, req);
        verify(checker).require("formulas.edit");
        verify(delegate).editFormula(1, req);
=======
        when(delegate.editFormula(1, req)).thenReturn(new FormulaDto(1, "Area2", "{const}*3", 1, "G"));
        proxy.editFormula(1, req);
        verify(checker).require("formulas.edit");
>>>>>>> Stashed changes
    }

    @Test
    void deleteFormula_requiresDeletePermission() {
        proxy.deleteFormula(1);
        verify(checker).require("formulas.delete");
        verify(delegate).deleteFormula(1);
    }
}
