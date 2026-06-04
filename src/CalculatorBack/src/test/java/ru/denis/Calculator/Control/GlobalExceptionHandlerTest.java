package ru.denis.Calculator.Control;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returns400WithMessage() {
        ResponseEntity<Map<String, String>> r =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody()).containsEntry("error", "bad input");
    }

    @Test
    void handleIllegalArgument_selectOnlyMessage_preserved() {
        ResponseEntity<Map<String, String>> r =
                handler.handleIllegalArgument(new IllegalArgumentException("Разрешены только SELECT-запросы"));
        assertThat(r.getBody()).containsEntry("error", "Разрешены только SELECT-запросы");
    }

    @Test
    void handleAccessDenied_returns403WithMessage() {
        ResponseEntity<Map<String, String>> r =
                handler.handleAccessDenied(new AccessDeniedException("Missing permission: sql.execute"));
        assertThat(r.getStatusCode().value()).isEqualTo(403);
        assertThat(r.getBody()).containsEntry("error", "Missing permission: sql.execute");
    }

    @Test
    void handleAccessDenied_noRoleMessage_returns403() {
        ResponseEntity<Map<String, String>> r =
                handler.handleAccessDenied(new AccessDeniedException("User has no role assigned"));
        assertThat(r.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void handleDataAccess_returns400WithCauseMessage() {
        DataAccessException ex = new DataAccessException("SQL error",
                new RuntimeException("column x does not exist")) {};
        ResponseEntity<Map<String, String>> r = handler.handleDataAccess(ex);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody().get("error")).contains("column x does not exist");
    }

    @Test
    void handleDataAccess_nullCauseMessage_returnsFallback() {
        DataAccessException ex = new DataAccessException("top", new RuntimeException((String) null)) {};
        ResponseEntity<Map<String, String>> r = handler.handleDataAccess(ex);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody()).containsKey("error");
    }
}
