package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

import java.util.List;

@Slf4j
@Service
@Primary
public class MaterialServiceDecorator implements IMaterialService {

    private final IMaterialService delegate;

    public MaterialServiceDecorator(
            @Qualifier("materialServiceProxy") IMaterialService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<MaterialDto> getAllMaterials(Integer groupId, String search) {
        log.info("getAllMaterials groupId={} search={} user={}", groupId, search, currentUser());
        try {
            List<MaterialDto> result = delegate.getAllMaterials(groupId, search);
            log.info("getAllMaterials: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllMaterials failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialDto getMaterialById(Integer id) {
        log.info("getMaterialById id={} user={}", id, currentUser());
        try {
            return delegate.getMaterialById(id);
        } catch (Exception e) {
            log.error("getMaterialById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialDto createMaterial(MaterialRequest request) {
        log.info("createMaterial name={} user={}", request.name(), currentUser());
        try {
            MaterialDto result = delegate.createMaterial(request);
            log.info("createMaterial created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createMaterial failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public MaterialDto editMaterial(Integer id, MaterialRequest request) {
        log.info("editMaterial id={} user={}", id, currentUser());
        try {
            MaterialDto result = delegate.editMaterial(id, request);
            log.info("editMaterial completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editMaterial failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteMaterial(Integer id) {
        log.info("deleteMaterial id={} user={}", id, currentUser());
        try {
            delegate.deleteMaterial(id);
            log.info("deleteMaterial completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteMaterial failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
