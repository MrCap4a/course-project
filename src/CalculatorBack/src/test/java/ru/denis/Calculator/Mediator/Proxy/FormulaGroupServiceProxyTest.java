package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaGroupServiceProxyTest {

    @Mock private IFormulaGroupService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private FormulaGroupServiceProxy proxy;

<<<<<<< Updated upstream
    private FormulaGroupDto dto() {
        return new FormulaGroupDto(1, "Geometry");
    }

=======
>>>>>>> Stashed changes
    @Test
    void getAllFormulaGroups_requiresViewPermission() {
        when(delegate.getAllFormulaGroups()).thenReturn(List.of());
        proxy.getAllFormulaGroups();
        verify(checker).require("formulas.view");
<<<<<<< Updated upstream
        verify(delegate).getAllFormulaGroups();
=======
>>>>>>> Stashed changes
    }

    @Test
    void getAllFormulaGroups_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("formulas.view");
        assertThatThrownBy(() -> proxy.getAllFormulaGroups()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getFormulaGroupById_requiresViewPermission() {
<<<<<<< Updated upstream
        when(delegate.getFormulaGroupById(1)).thenReturn(dto());
        proxy.getFormulaGroupById(1);
        verify(checker).require("formulas.view");
        verify(delegate).getFormulaGroupById(1);
=======
        when(delegate.getFormulaGroupById(1)).thenReturn(new FormulaGroupDto(1, "Geometry"));
        proxy.getFormulaGroupById(1);
        verify(checker).require("formulas.view");
>>>>>>> Stashed changes
    }

    @Test
    void createFormulaGroup_requiresCreatePermission() {
        FormulaGroupRequest req = new FormulaGroupRequest("Geometry");
<<<<<<< Updated upstream
        when(delegate.createFormulaGroup(req)).thenReturn(dto());
        proxy.createFormulaGroup(req);
        verify(checker).require("formulas.create");
        verify(delegate).createFormulaGroup(req);
=======
        when(delegate.createFormulaGroup(req)).thenReturn(new FormulaGroupDto(1, "Geometry"));
        proxy.createFormulaGroup(req);
        verify(checker).require("formulas.create");
>>>>>>> Stashed changes
    }

    @Test
    void editFormulaGroup_requiresEditPermission() {
        FormulaGroupRequest req = new FormulaGroupRequest("New");
<<<<<<< Updated upstream
        when(delegate.editFormulaGroup(1, req)).thenReturn(dto());
        proxy.editFormulaGroup(1, req);
        verify(checker).require("formulas.edit");
        verify(delegate).editFormulaGroup(1, req);
=======
        when(delegate.editFormulaGroup(1, req)).thenReturn(new FormulaGroupDto(1, "New"));
        proxy.editFormulaGroup(1, req);
        verify(checker).require("formulas.edit");
>>>>>>> Stashed changes
    }

    @Test
    void deleteFormulaGroup_requiresDeletePermission() {
        proxy.deleteFormulaGroup(1);
        verify(checker).require("formulas.delete");
        verify(delegate).deleteFormulaGroup(1);
    }
}
