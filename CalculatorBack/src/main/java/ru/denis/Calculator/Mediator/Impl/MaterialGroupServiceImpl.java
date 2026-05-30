package ru.denis.Calculator.Mediator.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;

import java.util.List;

@Service
public class MaterialGroupServiceImpl implements IMaterialGroupService {

    private static final String DEFAULT_GROUP_NAME = "Без группы";

    private final MaterialGroupRepository materialGroupRepository;
    private final MaterialRepository materialRepository;

    public MaterialGroupServiceImpl(MaterialGroupRepository materialGroupRepository,
                                    MaterialRepository materialRepository) {
        this.materialGroupRepository = materialGroupRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    public List<MaterialGroupDto> getAllMaterialGroups() {
        return materialGroupRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public MaterialGroupDto getMaterialGroupById(Integer id) {
        return toDto(materialGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + id)));
    }

    @Override
    public MaterialGroupDto createMaterialGroup(MaterialGroupRequest request) {
        MaterialGroup group = new MaterialGroup();
        group.setName(request.name());
        return toDto(materialGroupRepository.save(group));
    }

    @Override
    public MaterialGroupDto editMaterialGroup(Integer id, MaterialGroupRequest request) {
        MaterialGroup group = materialGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + id));
        group.setName(request.name());
        return toDto(materialGroupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteMaterialGroup(Integer id, DeleteGroupStrategy strategy, Integer targetGroupId) {
        MaterialGroup group = materialGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaterialGroup not found: " + id));

        List<Material> materials = materialRepository.findByGroup(group);

        switch (strategy) {
            case CASCADE -> materialRepository.deleteAll(materials);
            case DEFAULT -> {
                MaterialGroup defaultGroup = findOrCreateDefaultGroup(id);
                materials.forEach(m -> m.setGroup(defaultGroup));
                materialRepository.saveAll(materials);
            }
            case MOVE -> {
                if (targetGroupId == null) {
                    throw new RuntimeException("targetGroupId is required for MOVE strategy");
                }
                if (targetGroupId.equals(id)) {
                    throw new RuntimeException("Cannot move materials to the group being deleted");
                }
                MaterialGroup target = materialGroupRepository.findById(targetGroupId)
                        .orElseThrow(() -> new RuntimeException("Target MaterialGroup not found: " + targetGroupId));
                materials.forEach(m -> m.setGroup(target));
                materialRepository.saveAll(materials);
            }
        }

        materialGroupRepository.delete(group);
    }

    private MaterialGroup findOrCreateDefaultGroup(Integer excludeId) {
        return materialGroupRepository.findByName(DEFAULT_GROUP_NAME)
                .filter(g -> !g.getId().equals(excludeId))
                .orElseGet(() -> {
                    MaterialGroup defaultGroup = new MaterialGroup();
                    defaultGroup.setName(DEFAULT_GROUP_NAME);
                    return materialGroupRepository.save(defaultGroup);
                });
    }

    private MaterialGroupDto toDto(MaterialGroup group) {
        return new MaterialGroupDto(group.getId(), group.getName());
    }
}
