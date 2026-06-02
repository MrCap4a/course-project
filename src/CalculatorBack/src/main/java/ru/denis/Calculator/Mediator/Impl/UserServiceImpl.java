package ru.denis.Calculator.Mediator.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.UserRepository;
import ru.denis.Calculator.Foundation.UserRoleRepository;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Integer id) {
        return toDto(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id)));
    }

    @Override
    public UserDto createUser(RegisterRequest request) {
        UserRole role = resolveRole(request.roleId());

        User user = new User();
        user.setLogin(request.login());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setSuperAdmin(false);
        user.setRole(role);

        return toDto(userRepository.save(user));
    }

    @Override
    public UserDto editUser(Integer id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setRole(resolveRole(request.roleId()));

        return toDto(userRepository.save(user));
    }

    private UserRole resolveRole(Integer roleId) {
        if (roleId == null) {
            return null;
        }
        return userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
    }

    @Override
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        if (user.isSuperAdmin()) {
            throw new RuntimeException("Super admin cannot be deleted");
        }
        userRepository.delete(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getSurname(),
                user.isSuperAdmin(),
                user.getRole() != null ? user.getRole().getId() : null,
                user.getRole() != null ? user.getRole().getName() : null
        );
    }
}
