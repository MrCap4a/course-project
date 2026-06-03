package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialGroupServiceProxyTest {

    @Mock private IMaterialGroupService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private MaterialGroupServiceProxy proxy;

    private MaterialGroupDto dto() {
        return new MaterialGroupDto(1, "Metals");
    }

    @Test
    void getAllMaterialGroups_requiresViewPermission() {
        when(delegate.getAllMaterialGroups()).thenReturn(List.of());
        proxy.getAllMaterialGroups();
        verify(checker).require("materials.view");
        verify(delegate).getAllMaterialGroups();
    }

    @Test
    void getAllMaterialGroups_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("materials.view");
        assertThatThrownBy(() -> proxy.getAllMaterialGroups()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getMaterialGroupById_requiresViewPermission() {
        when(delegate.getMaterialGroupById(1)).thenReturn(dto());
        proxy.getMaterialGroupById(1);
        verify(checker).require("materials.view");
        verify(delegate).getMaterialGroupById(1);
    }

    @Test
    void createMaterialGroup_requiresCreatePermission() {
        MaterialGroupRequest req = new MaterialGroupRequest("Metals");
        when(delegate.createMaterialGroup(req)).thenReturn(dto());
        proxy.createMaterialGroup(req);
        verify(checker).require("materials.create");
        verify(delegate).createMaterialGroup(req);
    }

    @Test
    void editMaterialGroup_requiresEditPermission() {
        MaterialGroupRequest req = new MaterialGroupRequest("Alloys");
        when(delegate.editMaterialGroup(1, req)).thenReturn(dto());
        proxy.editMaterialGroup(1, req);
        verify(checker).require("materials.edit");
        verify(delegate).editMaterialGroup(1, req);
    }

    @Test
    void deleteMaterialGroup_cascade_requiresDeletePermission() {
        proxy.deleteMaterialGroup(1, DeleteGroupStrategy.CASCADE, null);
        verify(checker).require("materials.delete");
        verify(delegate).deleteMaterialGroup(1, DeleteGroupStrategy.CASCADE, null);
    }

    @Test
    void deleteMaterialGroup_move_requiresDeletePermission() {
        proxy.deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 2);
        verify(checker).require("materials.delete");
        verify(delegate).deleteMaterialGroup(1, DeleteGroupStrategy.MOVE, 2);
    }

    @Test
    void deleteMaterialGroup_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("materials.delete");
        assertThatThrownBy(() -> proxy.deleteMaterialGroup(1, DeleteGroupStrategy.DEFAULT, null))
                .isInstanceOf(AccessDeniedException.class);
    }
}
