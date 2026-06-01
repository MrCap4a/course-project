package ru.denis.Calculator.Mediator.Decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

@Slf4j
@Service
@Primary
public class UserServiceDecorator implements IUserService {

    private final IUserService delegate;

    public UserServiceDecorator(
            @Qualifier("userServiceProxy") IUserService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("getAllUsers user={}", currentUser());
        try {
            List<UserDto> result = delegate.getAllUsers();
            log.info("getAllUsers: fetched {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("getAllUsers failed user={}: {}", currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserDto getUserById(Integer id) {
        log.info("getUserById id={} user={}", id, currentUser());
        try {
            return delegate.getUserById(id);
        } catch (Exception e) {
            log.error("getUserById failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserDto createUser(RegisterRequest request) {
        log.info("createUser login={} user={}", request.login(), currentUser());
        try {
            UserDto result = delegate.createUser(request);
            log.info("createUser created id={} user={}", result.id(), currentUser());
            return result;
        } catch (Exception e) {
            log.error("createUser failed login={} user={}: {}", request.login(), currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public UserDto editUser(Integer id, RegisterRequest request) {
        log.info("editUser id={} user={}", id, currentUser());
        try {
            UserDto result = delegate.editUser(id, request);
            log.info("editUser completed id={} user={}", id, currentUser());
            return result;
        } catch (Exception e) {
            log.error("editUser failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteUser(Integer id) {
        log.info("deleteUser id={} user={}", id, currentUser());
        try {
            delegate.deleteUser(id);
            log.info("deleteUser completed id={} user={}", id, currentUser());
        } catch (Exception e) {
            log.error("deleteUser failed id={} user={}: {}", id, currentUser(), e.getMessage());
            throw e;
        }
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
