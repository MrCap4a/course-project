package ru.denis.Calculator.Control;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ── IllegalArgumentException → 400 ────────────────────────────────────────

    @Test
    void handleIllegalArgument_returns400WithErrorMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "bad input");
    }

    @Test
    void handleIllegalArgument_emptyMessage_returnsEmptyErrorField() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException(""));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("error");
    }

    // ── AccessDeniedException → 403 ───────────────────────────────────────────

    @Test
    void handleAccessDenied_returns403WithErrorMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("Missing permission: sql.execute"));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("error", "Missing permission: sql.execute");
    }

    @Test
    void handleAccessDenied_noRoleMessage_returns403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("User has no role assigned"));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    // ── DataAccessException → 400 ─────────────────────────────────────────────

    @Test
    void handleDataAccess_returns400WithPostgresMessage() {
        DataAccessException ex = new DataAccessException("SQL error", new RuntimeException("column x does not exist")) {
        };

        ResponseEntity<Map<String, String>> response = handler.handleDataAccess(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).contains("column x does not exist");
    }

    @Test
    void handleDataAccess_nullCauseMessage_returnsFallback() {
        DataAccessException ex = new DataAccessException("top level", new RuntimeException((String) null)) {
        };

        ResponseEntity<Map<String, String>> response = handler.handleDataAccess(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("error");
    }
}
