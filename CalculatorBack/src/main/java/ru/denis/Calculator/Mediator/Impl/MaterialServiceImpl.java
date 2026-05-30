package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

import java.util.List;

@Service
public class MaterialServiceImpl implements IMaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialGroupRepository materialGroupRepository;

    public MaterialServiceImpl(MaterialRepository materialRepository,
                               MaterialGroupRepository materialGroupRepository) {
        this.materialRepository = materialRepository;
        this.materialGroupRepository = materialGroupRepository;
    }

    @Override
    public List<MaterialDto> getAllMaterials(Integer groupId, String search) {
        boolean hasGroup = groupId != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasGroup && hasSearch) {
            MaterialGroup group = materialGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + groupId));
            return materialRepository.findByGroupAndNameContainingIgnoreCase(group, search)
                    .stream().map(this::toDto).toList();
        } else if (hasGroup) {
            MaterialGroup group = materialGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + groupId));
            return materialRepository.findByGroup(group).stream().map(this::toDto).toList();
        } else if (hasSearch) {
            return materialRepository.findByNameContainingIgnoreCase(search)
                    .stream().map(this::toDto).toList();
        } else {
            return materialRepository.findAll().stream().map(this::toDto).toList();
        }
    }

    @Override
    public MaterialDto getMaterialById(Integer id) {
        return toDto(materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id)));
    }

    @Override
    public MaterialDto createMaterial(MaterialRequest request) {
        MaterialGroup group = materialGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + request.groupId()));

        Material material = new Material();
        material.setName(request.name());
        material.setPrice(request.price());
        material.setUnits(request.units());
        material.setGroup(group);

        return toDto(materialRepository.save(material));
    }

    @Override
    public MaterialDto editMaterial(Integer id, MaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id));
        MaterialGroup group = materialGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + request.groupId()));

        material.setName(request.name());
        material.setPrice(request.price());
        material.setUnits(request.units());
        material.setGroup(group);

        return toDto(materialRepository.save(material));
    }

    @Override
    public void deleteMaterial(Integer id) {
        materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id));
        materialRepository.deleteById(id);
    }

    private MaterialDto toDto(Material material) {
        return new MaterialDto(
                material.getId(),
                material.getName(),
                material.getPrice(),
                material.getUnits(),
                material.getGroup().getId(),
                material.getGroup().getName()
        );
    }
}
