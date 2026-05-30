package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;

import java.util.List;

@Slf4j
@Service
@Primary
public class MaterialGroupServiceDecorator implements IMaterialGroupService {

    private final IMaterialGroupService delegate;

    public MaterialGroupServiceDecorator(
            @Qualifier("materialGroupServiceProxy") IMaterialGroupService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<MaterialGroupDto> getAllMaterialGroups() {
        log.info("getAllMaterialGroups user={}", currentUser());
        try {
            List<MaterialGroupDto> result = delegate.getAllMaterialGroups();
            log.info("getAllMaterialGroups: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllMaterialGroups failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialGroupDto getMaterialGroupById(Integer id) {
        log.info("getMaterialGroupById id={} user={}", id, currentUser());
        try {
            return delegate.getMaterialGroupById(id);
        } catch (Exception e) {
            log.error("getMaterialGroupById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialGroupDto createMaterialGroup(MaterialGroupRequest request) {
        log.info("createMaterialGroup name={} user={}", request.name(), currentUser());
        try {
            MaterialGroupDto result = delegate.createMaterialGroup(request);
            log.info("createMaterialGroup created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createMaterialGroup failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialGroupDto editMaterialGroup(Integer id, MaterialGroupRequest request) {
        log.info("editMaterialGroup id={} user={}", id, currentUser());
        try {
            MaterialGroupDto result = delegate.editMaterialGroup(id, request);
            log.info("editMaterialGroup completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editMaterialGroup failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteMaterialGroup(Integer id, DeleteGroupStrategy strategy, Integer targetGroupId) {
        log.info("deleteMaterialGroup id={} strategy={} targetGroupId={} user={}",
                id, strategy, targetGroupId, currentUser());
        try {
            delegate.deleteMaterialGroup(id, strategy, targetGroupId);
            log.info("deleteMaterialGroup completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteMaterialGroup failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
