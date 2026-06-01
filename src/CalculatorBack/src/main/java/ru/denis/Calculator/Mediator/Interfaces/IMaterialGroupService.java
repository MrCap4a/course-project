package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;

import java.util.List;

public interface IMaterialGroupService {
    List<MaterialGroupDto> getAllMaterialGroups();
    MaterialGroupDto getMaterialGroupById(Integer id);
    MaterialGroupDto createMaterialGroup(MaterialGroupRequest request);
    MaterialGroupDto editMaterialGroup(Integer id, MaterialGroupRequest request);
    void deleteMaterialGroup(Integer id, DeleteGroupStrategy strategy, Integer targetGroupId);
}
