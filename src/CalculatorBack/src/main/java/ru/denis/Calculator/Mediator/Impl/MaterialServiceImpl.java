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
import ru.denis.Calculator.Foundation.MaterialIdentityMap;
import ru.denis.Calculator.Foundation.MaterialRepository;
import ru.denis.Calculator.Mapper.MaterialMapper;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

@Service
public class MaterialServiceImpl implements IMaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialGroupRepository materialGroupRepository;
    private final MaterialMapper materialMapper;
    private final MaterialIdentityMap identityMap;

    public MaterialServiceImpl(MaterialRepository materialRepository,
                               MaterialGroupRepository materialGroupRepository,
                               MaterialMapper materialMapper,
                               MaterialIdentityMap identityMap) {
        this.materialRepository = materialRepository;
        this.materialGroupRepository = materialGroupRepository;
        this.materialMapper = materialMapper;
        this.identityMap = identityMap;
    }

    @Override
    public Page<MaterialDto> getAllMaterials(Integer groupId, String search, Pageable pageable) {
        boolean hasGroup = groupId != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasGroup && hasSearch) {
            MaterialGroup group = requireGroup(groupId);
            return materialRepository.findByGroupAndNameContainingIgnoreCase(group, search, pageable)
                    .map(materialMapper::toDto);
        } else if (hasGroup) {
            MaterialGroup group = requireGroup(groupId);
            return materialRepository.findByGroup(group, pageable).map(materialMapper::toDto);
        } else if (hasSearch) {
            return materialRepository.findByNameContainingIgnoreCase(search, pageable).map(materialMapper::toDto);
        } else {
            return materialRepository.findAll(pageable).map(materialMapper::toDto);
        }
    }

    @Override
    public MaterialDto getMaterialById(Integer id) {
        // Identity Map: сначала смотрим в кэш текущего запроса
        return identityMap.get(id)
                .map(materialMapper::toDto)
                .orElseGet(() -> {
                    Material material = materialRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Material not found: " + id));
                    identityMap.put(id, material);   // кладём в кэш для повторного использования
                    return materialMapper.toDto(material);
                });
    }

    @Override
    public MaterialDto createMaterial(MaterialRequest request) {
        MaterialGroup group = resolveGroup(request.groupId());
        Material material = materialMapper.toEntity(request, group);
        Material saved = materialRepository.save(material);
        identityMap.put(saved.getId(), saved);
        return materialMapper.toDto(saved);
    }

    @Override
    public MaterialDto editMaterial(Integer id, MaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id));
        MaterialGroup group = resolveGroup(request.groupId());
        materialMapper.applyRequest(material, request, group);
        Material saved = materialRepository.save(material);
        identityMap.put(saved.getId(), saved);   // обновляем кэш
        return materialMapper.toDto(saved);
    }

    @Override
    public void deleteMaterial(Integer id) {
        materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found: " + id));
        materialRepository.deleteById(id);
        identityMap.evict(id);   // удаляем из кэша
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
}
