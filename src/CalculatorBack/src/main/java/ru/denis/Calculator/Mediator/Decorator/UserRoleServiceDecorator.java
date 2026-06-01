package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Dto.Request.UserRoleRequest;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;

import java.util.List;

@Slf4j
@Service
@Primary
public class UserRoleServiceDecorator implements IUserRoleService {

    private final IUserRoleService delegate;

    public UserRoleServiceDecorator(
            @Qualifier("userRoleServiceProxy") IUserRoleService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<UserRoleDto> getAllRoles() {
        log.info("getAllRoles user={}", currentUser());
        try {
            List<UserRoleDto> result = delegate.getAllRoles();
            log.info("getAllRoles: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllRoles failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserRoleDto getRoleById(Integer id) {
        log.info("getRoleById id={} user={}", id, currentUser());
        try {
            return delegate.getRoleById(id);
        } catch (Exception e) {
            log.error("getRoleById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserRoleDto createRole(UserRoleRequest request) {
        log.info("createRole name={} user={}", request.name(), currentUser());
        try {
            UserRoleDto result = delegate.createRole(request);
            log.info("createRole created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createRole failed name={} user={}: {}", request.name(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserRoleDto editRole(Integer id, UserRoleRequest request) {
        log.info("editRole id={} user={}", id, currentUser());
        try {
            UserRoleDto result = delegate.editRole(id, request);
            log.info("editRole completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editRole failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteRole(Integer id) {
        log.info("deleteRole id={} user={}", id, currentUser());
        try {
            delegate.deleteRole(id);
            log.info("deleteRole completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteRole failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
