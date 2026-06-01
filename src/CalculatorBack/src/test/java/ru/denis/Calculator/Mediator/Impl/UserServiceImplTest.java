package ru.denis.Calculator.Mediator.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.Request.RegisterRequest;
import ru.denis.Calculator.Entity.User;
import ru.denis.Calculator.Entity.UserRole;
import ru.denis.Calculator.Foundation.UserRepository;
import ru.denis.Calculator.Foundation.UserRoleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl service;

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsMappedList() {
        UserRole role = role(1, "Admin");
        when(userRepository.findAll()).thenReturn(List.of(user(1, "alice", "Alice", "Smith", false, role)));

        List<UserDto> result = service.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("alice");
        assertThat(result.get(0).roleName()).isEqualTo("Admin");
    }

    @Test
    void getAllUsers_userWithNoRole_roleFieldsAreNull() {
        when(userRepository.findAll()).thenReturn(List.of(user(1, "alice", "Alice", "Smith", false, null)));

        List<UserDto> result = service.getAllUsers();

        assertThat(result.get(0).roleId()).isNull();
        assertThat(result.get(0).roleName()).isNull();
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsDto() {
        UserRole role = role(1, "Admin");
        when(userRepository.findById(1)).thenReturn(Optional.of(user(1, "alice", "Alice", "Smith", false, role)));

        UserDto dto = service.getUserById(1);

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.login()).isEqualTo("alice");
        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.surname()).isEqualTo("Smith");
        assertThat(dto.superAdmin()).isFalse();
        assertThat(dto.roleId()).isEqualTo(1);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 99");
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_success_encodesPasswordAndSaves() {
        UserRole role = role(1, "Admin");
        RegisterRequest req = new RegisterRequest("bob", "secret", "Bob", "Jones", 1);
        User saved = user(5, "bob", "Bob", "Jones", false, role);
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(saved);

        UserDto dto = service.createUser(req);

        assertThat(dto.id()).isEqualTo(5);
        assertThat(dto.login()).isEqualTo("bob");
        assertThat(dto.superAdmin()).isFalse();
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any());
    }

    @Test
    void createUser_roleNotFound_throws() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createUser(new RegisterRequest("bob", "pass", "Bob", "J", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: 99");
    }

    // ── editUser ──────────────────────────────────────────────────────────────

    @Test
    void editUser_withNewPassword_encodesAndUpdates() {
        UserRole role = role(1, "Admin");
        User existing = user(1, "alice", "Alice", "Smith", false, role);
        User updated = user(1, "alice2", "AliceNew", "SmithNew", false, role);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");
        when(userRepository.save(existing)).thenReturn(updated);

        UserDto dto = service.editUser(1, new RegisterRequest("alice2", "newpass", "AliceNew", "SmithNew", 1));

        assertThat(dto.login()).isEqualTo("alice2");
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void editUser_withBlankPassword_doesNotEncodePassword() {
        UserRole role = role(1, "Admin");
        User existing = user(1, "alice", "Alice", "Smith", false, role);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role));
        when(userRepository.save(existing)).thenReturn(existing);

        service.editUser(1, new RegisterRequest("alice", "  ", "Alice", "Smith", 1));

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void editUser_withNullPassword_doesNotEncodePassword() {
        UserRole role = role(1, "Admin");
        User existing = user(1, "alice", "Alice", "Smith", false, role);
        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRoleRepository.findById(1)).thenReturn(Optional.of(role));
        when(userRepository.save(existing)).thenReturn(existing);

        service.editUser(1, new RegisterRequest("alice", null, "Alice", "Smith", 1));

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void editUser_userNotFound_throws() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editUser(99, new RegisterRequest("x", "p", "X", "Y", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 99");
    }

    @Test
    void editUser_roleNotFound_throws() {
        UserRole role = role(1, "Admin");
        when(userRepository.findById(1)).thenReturn(Optional.of(user(1, "alice", "Alice", "Smith", false, role)));
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editUser(1, new RegisterRequest("alice", "p", "Alice", "Smith", 99)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: 99");
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_normalUser_deletesSuccessfully() {
        UserRole role = role(1, "Admin");
        User u = user(1, "alice", "Alice", "Smith", false, role);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        service.deleteUser(1);

        verify(userRepository).delete(u);
    }

    @Test
    void deleteUser_superAdmin_throws() {
        UserRole role = role(1, "Admin");
        User admin = user(1, "root", "Root", "User", true, role);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.deleteUser(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Super admin cannot be deleted");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 99");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserRole role(int id, String name) {
        UserRole r = new UserRole();
        r.setId(id);
        r.setName(name);
        return r;
    }

    private User user(int id, String login, String name, String surname, boolean superAdmin, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setLogin(login);
        u.setName(name);
        u.setSurname(surname);
        u.setSuperAdmin(superAdmin);
        u.setRole(role);
        return u;
    }
}
