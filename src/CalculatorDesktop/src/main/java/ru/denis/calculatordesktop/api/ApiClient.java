package ru.denis.calculatordesktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.denis.calculatordesktop.api.dto.*;
import ru.denis.calculatordesktop.session.SessionManager;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private final HttpClient http;
    private final ObjectMapper mapper;
    private String baseUrl = "http://localhost:8080/api/v1";

    private ApiClient() {
        http = HttpClient.newHttpClient();
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() { return INSTANCE; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String url) { this.baseUrl = url; }

    // =========================================================================
    // Auth
    // =========================================================================

    public LoginResponse login(String login, String password) throws ApiException {
        return post("/auth/login", Map.of("login", login, "password", password),
                new TypeReference<>() {}, false);
    }

    // =========================================================================
    // Current user
    // =========================================================================

    public CurrentUserDto getCurrentUser() throws ApiException {
        return get("/users/me", new TypeReference<>() {});
    }

    // =========================================================================
    // Users
    // =========================================================================

    public List<UserDto> getUsers() throws ApiException {
        return get("/users", new TypeReference<>() {});
    }

    public UserDto createUser(String login, String password, String name, String surname, Integer roleId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("login", login);
        body.put("password", password);
        body.put("name", name);
        body.put("surname", surname);
        body.put("roleId", roleId);
        return post("/users", body, new TypeReference<>() {});
    }

    public UserDto updateUser(Integer id, String login, String password,
                              String name, String surname, Integer roleId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("login", login);
        if (password != null && !password.isBlank()) body.put("password", password);
        body.put("name", name);
        body.put("surname", surname);
        body.put("roleId", roleId);
        return put("/users/" + id, body, new TypeReference<>() {});
    }

    public void deleteUser(Integer id) throws ApiException {
        delete("/users/" + id);
    }

    // =========================================================================
    // Roles
    // =========================================================================

    public List<UserRoleDto> getRoles() throws ApiException {
        return get("/roles", new TypeReference<>() {});
    }

    public UserRoleDto createRole(String name, List<Integer> permissionIds) throws ApiException {
        return post("/roles", Map.of("name", name, "permissionIds", permissionIds),
                new TypeReference<>() {});
    }

    public UserRoleDto updateRole(Integer id, String name, List<Integer> permissionIds) throws ApiException {
        return put("/roles/" + id, Map.of("name", name, "permissionIds", permissionIds),
                new TypeReference<>() {});
    }

    public void deleteRole(Integer id) throws ApiException {
        delete("/roles/" + id);
    }

    // =========================================================================
    // Permissions
    // =========================================================================

    public List<PermissionDto> getPermissions() throws ApiException {
        return get("/permissions", new TypeReference<>() {});
    }

    // =========================================================================
    // Materials
    // =========================================================================

    public List<MaterialDto> getMaterials(Integer groupId, String search) throws ApiException {
        StringBuilder url = new StringBuilder("/materials");
        String sep = "?";
        if (groupId != null) {
            url.append(sep).append("groupId=").append(groupId);
            sep = "&";
        }
        if (search != null && !search.isBlank()) {
            url.append(sep).append("search=")
               .append(URLEncoder.encode(search, StandardCharsets.UTF_8));
        }
        return get(url.toString(), new TypeReference<>() {});
    }

    public MaterialDto createMaterial(String name, BigDecimal price, String units, Integer groupId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("units", units);
        body.put("groupId", groupId);
        return post("/materials", body, new TypeReference<>() {});
    }

    public MaterialDto updateMaterial(Integer id, String name, BigDecimal price,
                                      String units, Integer groupId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("units", units);
        body.put("groupId", groupId);
        return put("/materials/" + id, body, new TypeReference<>() {});
    }

    public void deleteMaterial(Integer id) throws ApiException {
        delete("/materials/" + id);
    }

    // =========================================================================
    // Material Groups
    // =========================================================================

    public List<MaterialGroupDto> getMaterialGroups() throws ApiException {
        return get("/material-groups", new TypeReference<>() {});
    }

    public MaterialGroupDto createMaterialGroup(String name) throws ApiException {
        return post("/material-groups", Map.of("name", name), new TypeReference<>() {});
    }

    public MaterialGroupDto updateMaterialGroup(Integer id, String name) throws ApiException {
        return put("/material-groups/" + id, Map.of("name", name), new TypeReference<>() {});
    }

    public void deleteMaterialGroup(Integer id, String strategy, Integer targetGroupId) throws ApiException {
        StringBuilder url = new StringBuilder("/material-groups/").append(id)
                .append("?strategy=").append(strategy);
        if (targetGroupId != null) url.append("&targetGroupId=").append(targetGroupId);
        delete(url.toString());
    }

    // =========================================================================
    // Formulas
    // =========================================================================

    public List<FormulaDto> getFormulas() throws ApiException {
        return get("/formulas", new TypeReference<>() {});
    }

    public FormulaDto createFormula(String name, String expression, Integer groupId) throws ApiException {
        return post("/formulas", Map.of("name", name, "expression", expression, "groupId", groupId),
                new TypeReference<>() {});
    }

    public FormulaDto updateFormula(Integer id, String name, String expression, Integer groupId) throws ApiException {
        return put("/formulas/" + id,
                Map.of("name", name, "expression", expression, "groupId", groupId),
                new TypeReference<>() {});
    }

    public void deleteFormula(Integer id) throws ApiException {
        delete("/formulas/" + id);
    }

    // =========================================================================
    // Formula Groups
    // =========================================================================

    public List<FormulaGroupDto> getFormulaGroups() throws ApiException {
        return get("/formula-groups", new TypeReference<>() {});
    }

    public FormulaGroupDto createFormulaGroup(String name) throws ApiException {
        return post("/formula-groups", Map.of("name", name), new TypeReference<>() {});
    }

    public FormulaGroupDto updateFormulaGroup(Integer id, String name) throws ApiException {
        return put("/formula-groups/" + id, Map.of("name", name), new TypeReference<>() {});
    }

    public void deleteFormulaGroup(Integer id) throws ApiException {
        delete("/formula-groups/" + id);
    }

    // =========================================================================
    // Calculations
    // =========================================================================

    public List<CalculationDto> getCalculations() throws ApiException {
        return get("/calculations", new TypeReference<>() {});
    }

    public CalculationDto createCalculation(String name, Integer formulaId,
                                            List<Map<String, Object>> items) throws ApiException {
        return post("/calculations",
                Map.of("name", name, "formulaId", formulaId, "items", items),
                new TypeReference<>() {});
    }

    public CalculationDto updateCalculation(Integer id, String name, Integer formulaId,
                                            List<Map<String, Object>> items) throws ApiException {
        return put("/calculations/" + id,
                Map.of("name", name, "formulaId", formulaId, "items", items),
                new TypeReference<>() {});
    }

    public void deleteCalculation(Integer id) throws ApiException {
        delete("/calculations/" + id);
    }

    // =========================================================================
    // HTTP internals
    // =========================================================================

    private <T> T get(String path, TypeReference<T> type) throws ApiException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Authorization", bearer())
                .GET()
                .build();
        return execute(req, type);
    }

    private <T> T post(String path, Object body, TypeReference<T> type) throws ApiException {
        return post(path, body, type, true);
    }

    private <T> T post(String path, Object body, TypeReference<T> type, boolean auth) throws ApiException {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            if (auth) b.header("Authorization", bearer());
            return execute(b.build(), type);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, e.getMessage());
        }
    }

    private <T> T put(String path, Object body, TypeReference<T> type) throws ApiException {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .header("Authorization", bearer())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return execute(req, type);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, e.getMessage());
        }
    }

    private void delete(String path) throws ApiException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", bearer())
                .DELETE()
                .build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new ApiException(resp.statusCode(), extractMessage(resp.body()));
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, e.getMessage());
        }
    }

    private <T> T execute(HttpRequest req, TypeReference<T> type) throws ApiException {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new ApiException(resp.statusCode(), extractMessage(resp.body()));
            }
            String body = resp.body();
            if (body == null || body.isBlank()) return null;
            return mapper.readValue(body, type);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, e.getMessage());
        }
    }

    private String bearer() {
        String token = SessionManager.getInstance().getToken();
        return token != null ? "Bearer " + token : "";
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) return "Ошибка сервера";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.readValue(body, Map.class);
            Object msg = map.get("message");
            if (msg != null) return msg.toString();
            Object err = map.get("error");
            if (err != null) return err.toString();
        } catch (Exception ignored) {}
        return body.length() > 300 ? body.substring(0, 300) : body;
    }
}
