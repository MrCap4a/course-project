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

    private FormulaGroupDto dto() {
        return new FormulaGroupDto(1, "Geometry");
    }

    @Test
    void getAllFormulaGroups_requiresViewPermission() {
        when(delegate.getAllFormulaGroups()).thenReturn(List.of());
        proxy.getAllFormulaGroups();
        verify(checker).require("formulas.view");
        verify(delegate).getAllFormulaGroups();
    }

    @Test
    void getAllFormulaGroups_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("formulas.view");
        assertThatThrownBy(() -> proxy.getAllFormulaGroups()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getFormulaGroupById_requiresViewPermission() {
        when(delegate.getFormulaGroupById(1)).thenReturn(dto());
        proxy.getFormulaGroupById(1);
        verify(checker).require("formulas.view");
        verify(delegate).getFormulaGroupById(1);
    }

    @Test
    void createFormulaGroup_requiresCreatePermission() {
        FormulaGroupRequest req = new FormulaGroupRequest("Geometry");
        when(delegate.createFormulaGroup(req)).thenReturn(dto());
        proxy.createFormulaGroup(req);
        verify(checker).require("formulas.create");
        verify(delegate).createFormulaGroup(req);
    }

    @Test
    void editFormulaGroup_requiresEditPermission() {
        FormulaGroupRequest req = new FormulaGroupRequest("New");
        when(delegate.editFormulaGroup(1, req)).thenReturn(dto());
        proxy.editFormulaGroup(1, req);
        verify(checker).require("formulas.edit");
        verify(delegate).editFormulaGroup(1, req);
    }

    @Test
    void deleteFormulaGroup_requiresDeletePermission() {
        proxy.deleteFormulaGroup(1);
        verify(checker).require("formulas.delete");
        verify(delegate).deleteFormulaGroup(1);
    }
}
