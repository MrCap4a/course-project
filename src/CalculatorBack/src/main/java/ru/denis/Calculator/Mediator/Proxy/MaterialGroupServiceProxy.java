package ru.denis.Calculator.Mediator.Proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;

import java.util.List;

@Service
public class MaterialGroupServiceProxy implements IMaterialGroupService {

    private final IMaterialGroupService delegate;
    private final PermissionChecker checker;

    public MaterialGroupServiceProxy(
            @Qualifier("materialGroupServiceImpl") IMaterialGroupService delegate,
            PermissionChecker checker) {
        this.delegate = delegate;
        this.checker = checker;
    }

    @Override
    public List<MaterialGroupDto> getAllMaterialGroups() {
        checker.require("materials.view");
        return delegate.getAllMaterialGroups();
    }

    @Override
    public MaterialGroupDto getMaterialGroupById(Integer id) {
        checker.require("materials.view");
        return delegate.getMaterialGroupById(id);
    }

    @Override
    public MaterialGroupDto createMaterialGroup(MaterialGroupRequest request) {
        checker.require("materials.create");
        return delegate.createMaterialGroup(request);
    }

    @Override
    public MaterialGroupDto editMaterialGroup(Integer id, MaterialGroupRequest request) {
        checker.require("materials.edit");
        return delegate.editMaterialGroup(id, request);
    }

    @Override
    public void deleteMaterialGroup(Integer id, DeleteGroupStrategy strategy, Integer targetGroupId) {
        checker.require("materials.delete");
        delegate.deleteMaterialGroup(id, strategy, targetGroupId);
    }
}
