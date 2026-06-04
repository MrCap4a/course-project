package ru.denis.Calculator.Mediator.Proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceProxyTest {

    @Mock private IMaterialService delegate;
    @Mock private PermissionChecker checker;
    @InjectMocks private MaterialServiceProxy proxy;

<<<<<<< Updated upstream
    private MaterialDto dto() {
        return new MaterialDto(1, "Steel", new BigDecimal("100"), "kg", 1, "Metals");
    }

=======
>>>>>>> Stashed changes
    @Test
    @SuppressWarnings("unchecked")
    void getAllMaterials_requiresViewPermission() {
        Page<MaterialDto> page = mock(Page.class);
        when(delegate.getAllMaterials(null, null, Pageable.unpaged())).thenReturn(page);
        proxy.getAllMaterials(null, null, Pageable.unpaged());
        verify(checker).require("materials.view");
<<<<<<< Updated upstream
        verify(delegate).getAllMaterials(null, null, Pageable.unpaged());
=======
>>>>>>> Stashed changes
    }

    @Test
    void getAllMaterials_denied_throwsAccessDenied() {
        doThrow(new AccessDeniedException("no")).when(checker).require("materials.view");
        assertThatThrownBy(() -> proxy.getAllMaterials(null, null, Pageable.unpaged()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getMaterialById_requiresViewPermission() {
<<<<<<< Updated upstream
        when(delegate.getMaterialById(1)).thenReturn(dto());
        proxy.getMaterialById(1);
        verify(checker).require("materials.view");
        verify(delegate).getMaterialById(1);
=======
        when(delegate.getMaterialById(1)).thenReturn(
                new MaterialDto(1, "Steel", BigDecimal.TEN, "kg", 1, "Metals"));
        proxy.getMaterialById(1);
        verify(checker).require("materials.view");
>>>>>>> Stashed changes
    }

    @Test
    void createMaterial_requiresCreatePermission() {
<<<<<<< Updated upstream
        MaterialRequest req = new MaterialRequest("Steel", new BigDecimal("100"), "kg", 1);
        when(delegate.createMaterial(req)).thenReturn(dto());
        proxy.createMaterial(req);
        verify(checker).require("materials.create");
        verify(delegate).createMaterial(req);
=======
        MaterialRequest req = new MaterialRequest("Steel", BigDecimal.TEN, "kg", 1);
        when(delegate.createMaterial(req)).thenReturn(
                new MaterialDto(1, "Steel", BigDecimal.TEN, "kg", 1, "Metals"));
        proxy.createMaterial(req);
        verify(checker).require("materials.create");
>>>>>>> Stashed changes
    }

    @Test
    void editMaterial_requiresEditPermission() {
<<<<<<< Updated upstream
        MaterialRequest req = new MaterialRequest("Steel2", new BigDecimal("200"), "kg", 1);
        when(delegate.editMaterial(1, req)).thenReturn(dto());
        proxy.editMaterial(1, req);
        verify(checker).require("materials.edit");
        verify(delegate).editMaterial(1, req);
=======
        MaterialRequest req = new MaterialRequest("Steel2", BigDecimal.TEN, "kg", 1);
        when(delegate.editMaterial(1, req)).thenReturn(
                new MaterialDto(1, "Steel2", BigDecimal.TEN, "kg", 1, "Metals"));
        proxy.editMaterial(1, req);
        verify(checker).require("materials.edit");
>>>>>>> Stashed changes
    }

    @Test
    void deleteMaterial_requiresDeletePermission() {
        proxy.deleteMaterial(1);
        verify(checker).require("materials.delete");
        verify(delegate).deleteMaterial(1);
    }
}
