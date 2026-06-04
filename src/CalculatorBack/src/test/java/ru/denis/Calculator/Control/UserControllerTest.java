package ru.denis.Calculator.Control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.denis.Calculator.Dto.PermissionDto;
import ru.denis.Calculator.Dto.UserDto;
import ru.denis.Calculator.Dto.UserRoleDto;
import ru.denis.Calculator.Entity.Permission;
import ru.denis.Calculator.Foundation.PermissionRepository;
import ru.denis.Calculator.Foundation.UserRepository;
import ru.denis.Calculator.Mediator.Interfaces.IUserRoleService;
import ru.denis.Calculator.Mediator.Interfaces.IUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock IUserService userService;
    @Mock IUserRoleService userRoleService;
    @Mock PermissionRepository permissionRepository;
<<<<<<< Updated upstream
    @Mock UserRepository userRepository;
=======
    @Mock ru.denis.Calculator.Foundation.UserRepository userRepository;
>>>>>>> Stashed changes

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService, userRoleService, permissionRepository, userRepository))
                .build();
    }

    private UserDto sampleUser(int id, String login) {
        return new UserDto(id, login, "Name", "Surname", false, 1, "Admin");
    }

    private UserRoleDto sampleRole(int id, String name) {
        return new UserRoleDto(id, name, List.of(new PermissionDto(1, "materials.view")));
    }

    // ── GET /permissions ──────────────────────────────────────────────────────

    @Test
    void getAllPermissions_returns200WithList() throws Exception {
        Permission p1 = new Permission(); p1.setId(1); p1.setName("materials.view");
        Permission p2 = new Permission(); p2.setId(2); p2.setName("formulas.view");
        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("materials.view"))
                .andExpect(jsonPath("$[1].name").value("formulas.view"));
    }

    @Test
    void getAllPermissions_empty_returnsEmptyList() throws Exception {
        when(permissionRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /users ────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returns200WithList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser(1, "alice"), sampleUser(2, "bob")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].login").value("alice"))
                .andExpect(jsonPath("$[0].name").value("Name"))
                .andExpect(jsonPath("$[0].roleId").value(1))
                .andExpect(jsonPath("$[0].roleName").value("Admin"))
                .andExpect(jsonPath("$[1].login").value("bob"));
    }

    // ── POST /users ───────────────────────────────────────────────────────────

    @Test
    void createUser_validRequest_returns201() throws Exception {
        when(userService.createUser(any())).thenReturn(sampleUser(5, "charlie"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"charlie\",\"password\":\"pass\",\"name\":\"Charlie\",\"surname\":\"Brown\",\"roleId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.login").value("charlie"));
    }

    @Test
    void createUser_roleNotFound_propagatesException() {
        when(userService.createUser(any()))
                .thenThrow(new RuntimeException("Role not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"x\",\"password\":\"p\",\"name\":\"N\",\"surname\":\"S\",\"roleId\":99}")))
                .hasRootCauseMessage("Role not found: 99");
    }

    // ── PUT /users/{id} ───────────────────────────────────────────────────────

    @Test
    void editUser_validRequest_returns200() throws Exception {
        when(userService.editUser(eq(1), any())).thenReturn(sampleUser(1, "alice_updated"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alice_updated\",\"password\":\"\",\"name\":\"Alice\",\"surname\":\"Smith\",\"roleId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("alice_updated"));
    }

    @Test
    void editUser_notFound_propagatesException() {
        when(userService.editUser(eq(99), any()))
                .thenThrow(new RuntimeException("User not found: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"x\",\"password\":\"p\",\"name\":\"N\",\"surname\":\"S\",\"roleId\":1}")))
                .hasRootCauseMessage("User not found: 99");
    }

    // ── DELETE /users/{id} ────────────────────────────────────────────────────

    @Test
    void deleteUser_normalUser_returns204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1);
    }

    @Test
    void deleteUser_superAdmin_propagatesException() {
        doThrow(new RuntimeException("Super admin cannot be deleted"))
                .when(userService).deleteUser(1);

        assertThatThrownBy(() -> mockMvc.perform(delete("/users/1")))
                .hasRootCauseMessage("Super admin cannot be deleted");
    }

    // ── GET /roles ────────────────────────────────────────────────────────────

    @Test
    void getAllRoles_returns200WithList() throws Exception {
        when(userRoleService.getAllRoles())
                .thenReturn(List.of(sampleRole(1, "Admin"), sampleRole(2, "Viewer")));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Admin"))
                .andExpect(jsonPath("$[0].permissions[0].name").value("materials.view"))
                .andExpect(jsonPath("$[1].name").value("Viewer"));
    }

    // ── POST /roles ───────────────────────────────────────────────────────────

    @Test
    void createRole_validRequest_returns201() throws Exception {
        when(userRoleService.createRole(any())).thenReturn(sampleRole(3, "Editor"));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Editor\",\"permissionIds\":[1,2]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Editor"));
    }

    // ── PUT /roles/{id} ───────────────────────────────────────────────────────

    @Test
    void editRole_validRequest_returns200() throws Exception {
        when(userRoleService.editRole(eq(1), any())).thenReturn(sampleRole(1, "UpdatedAdmin"));

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdatedAdmin\",\"permissionIds\":[1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedAdmin"));
    }

    // ── DELETE /roles/{id} ────────────────────────────────────────────────────

    @Test
    void deleteRole_exists_returns204() throws Exception {
        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isNoContent());

        verify(userRoleService).deleteRole(1);
    }

    @Test
    void deleteRole_notFound_propagatesException() {
        doThrow(new RuntimeException("Role not found: 99"))
                .when(userRoleService).deleteRole(99);

        assertThatThrownBy(() -> mockMvc.perform(delete("/roles/99")))
                .hasRootCauseMessage("Role not found: 99");
    }
}
