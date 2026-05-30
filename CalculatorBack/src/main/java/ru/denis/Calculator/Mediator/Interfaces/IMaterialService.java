package ru.denis.Calculator.Mediator.Interfaces;

import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;

import java.util.List;

public interface IMaterialService {
    List<MaterialDto> getAllMaterials(Integer groupId, String search);
    MaterialDto getMaterialById(Integer id);
    MaterialDto createMaterial(MaterialRequest request);
    MaterialDto editMaterial(Integer id, MaterialRequest request);
    void deleteMaterial(Integer id);
}
