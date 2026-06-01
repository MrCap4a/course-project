package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

@Service
public class MaterialServiceProxy implements IMaterialService {

    private final IMaterialService delegate;
    private final PermissionChecker checker;

    public MaterialServiceProxy(
            @Qualifier("materialServiceImpl") IMaterialService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    @Override
    public Page<MaterialDto> getAllMaterials(Integer groupId, String search, Pageable pageable) {
        checker.require("materials.view");
        return delegate.getAllMaterials(groupId, search, pageable);
    }

    @Override
    public MaterialDto getMaterialById(Integer id) {
        checker.require("materials.view");
        return delegate.getMaterialById(id);
    }

    @Override
    public MaterialDto createMaterial(MaterialRequest request) {
        checker.require("materials.create");
        return delegate.createMaterial(request);
    }

    @Override
    public MaterialDto editMaterial(Integer id, MaterialRequest request) {
        checker.require("materials.edit");
        return delegate.editMaterial(id, request);
    }

    @Override
    public void deleteMaterial(Integer id) {
        checker.require("materials.delete");
        delegate.deleteMaterial(id);
    }
}
