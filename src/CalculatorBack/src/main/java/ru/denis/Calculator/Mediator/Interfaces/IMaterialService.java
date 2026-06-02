package ru.denis.Calculator.Mediator.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;

public interface IMaterialService {

    Page<MaterialDto> getAllMaterials(Integer groupId, String search, Pageable pageable);

    MaterialDto getMaterialById(Integer id);

    MaterialDto createMaterial(MaterialRequest request);

    MaterialDto editMaterial(Integer id, MaterialRequest request);

    void deleteMaterial(Integer id);

}
