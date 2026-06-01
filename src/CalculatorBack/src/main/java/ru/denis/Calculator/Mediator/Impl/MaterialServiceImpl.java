package ru.denis.Calculator.Mediator.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Config.DataInitializer;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

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
    public Page<MaterialDto> getAllMaterials(Integer groupId, String search, Pageable pageable) {
        boolean hasGroup = groupId != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasGroup && hasSearch) {
            MaterialGroup group = requireGroup(groupId);
            return materialRepository.findByGroupAndNameContainingIgnoreCase(group, search, pageable)
                    .map(this::toDto);
        } else if (hasGroup) {
            MaterialGroup group = requireGroup(groupId);
            return materialRepository.findByGroup(group, pageable).map(this::toDto);
        } else if (hasSearch) {
            return materialRepository.findByNameContainingIgnoreCase(search, pageable).map(this::toDto);
        } else {
            return materialRepository.findAll(pageable).map(this::toDto);
        }
    }

    @Override
    public MaterialDto getMaterialById(Integer id) {
        return toDto(materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id)));
    }

    @Override
    public MaterialDto createMaterial(MaterialRequest request) {
        MaterialGroup group = resolveGroup(request.groupId());
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
        MaterialGroup group = resolveGroup(request.groupId());
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

    private MaterialGroup requireGroup(Integer groupId) {
        return materialGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + groupId));
    }

    private MaterialGroup resolveGroup(Integer groupId) {
        if (groupId != null) {
            return requireGroup(groupId);
        }
        return materialGroupRepository.findByNameIgnoreCase(DataInitializer.DEFAULT_GROUP_NAME)
                .orElseGet(() -> {
                    MaterialGroup def = new MaterialGroup();
                    def.setName(DataInitializer.DEFAULT_GROUP_NAME);
                    return materialGroupRepository.save(def);
                });
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
