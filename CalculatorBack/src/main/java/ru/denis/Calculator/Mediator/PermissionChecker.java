package ru.denis.Calculator.Mediator;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Foundation.UserRepository;

@Component
public class PermissionChecker {

    private final UserRepository userRepository;

    public PermissionChecker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void require(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User user = userRepository.findByLoginWithPermissions(login)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + login));

        if (user.isSuperAdmin()) return;

        if (user.getRole() == null) {
            throw new AccessDeniedException("User has no role assigned");
        }

        boolean hasPermission = user.getRole().getPermissions().stream()
                .anyMatch(p -> p.getName().equals(permission));

        if (!hasPermission) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
    }
}
