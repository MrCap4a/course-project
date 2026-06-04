package ru.denis.Calculator.Aspect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import ru.denis.Calculator.Entity.AuditLog;
import ru.denis.Calculator.Foundation.AuditLogRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLoggingInterceptorTest {

    @Mock private AuditLogRepository auditLogRepository;
    @InjectMocks private AuditLoggingInterceptor interceptor;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void preHandle_withHandlerMethod_setsStartAndAction() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        HandlerMethod handler = mock(HandlerMethod.class);
        when(handler.getShortLogMessage()).thenReturn("SqlController#execute");

        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), handler);

        assertThat(result).isTrue();
        assertThat(req.getAttribute("audit_start")).isNotNull();
        assertThat(req.getAttribute("audit_action")).isEqualTo("SqlController#execute");
    }

    @Test
    void preHandle_withNonHandlerMethod_setsNoAttributes() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(result).isTrue();
        assertThat(req.getAttribute("audit_start")).isNull();
    }

    @Test
    void afterCompletion_withAuditAction_savesLog() {
        MockHttpServletRequest req = requestWithAction("/api/v1/sql/execute", "POST", "10.0.0.1");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(200);
        setupAuth("alice");

        interceptor.afterCompletion(req, resp, new Object(), null);

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getUsername()).isEqualTo("alice");
        assertThat(cap.getValue().getStatus()).isEqualTo("SUCCESS");
        assertThat(cap.getValue().getEndpoint()).isEqualTo("/api/v1/sql/execute");
        assertThat(cap.getValue().getTimestamp()).isNotNull();
    }

    @Test
    void afterCompletion_withException_setsErrorStatus() {
        MockHttpServletRequest req = requestWithAction("/api/v1/users", "DELETE", "10.0.0.2");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(500);
        setupAuth("bob");

        interceptor.afterCompletion(req, resp, new Object(), new RuntimeException("boom"));

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("ERROR");
    }

    @Test
    void afterCompletion_with500Status_setsErrorStatus() {
        MockHttpServletRequest req = requestWithAction("/api/v1/sql/execute", "POST", "10.0.0.3");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(500);
        setupAuth("carol");

        interceptor.afterCompletion(req, resp, new Object(), null);

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("ERROR");
    }

    @Test
    void afterCompletion_with4xxStatus_setsSuccessStatus() {
        MockHttpServletRequest req = requestWithAction("/api/v1/materials", "GET", "10.0.0.4");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(404);
        setupAuth("dave");

        interceptor.afterCompletion(req, resp, new Object(), null);

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void afterCompletion_noAuthentication_usesAnonymous() {
        MockHttpServletRequest req = requestWithAction("/api/v1/auth/login", "POST", "10.0.0.5");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        resp.setStatus(200);

        interceptor.afterCompletion(req, resp, new Object(), null);

        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        assertThat(cap.getValue().getUsername()).isEqualTo("anonymous");
    }

    @Test
    void afterCompletion_noAuditAction_doesNotSave() {
        interceptor.afterCompletion(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new Object(), null);
        verify(auditLogRepository, never()).save(any());
    }

    private MockHttpServletRequest requestWithAction(String uri, String method, String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setAttribute("audit_action", "SomeController#method");
        req.setRequestURI(uri);
        req.setMethod(method);
        req.setRemoteAddr(ip);
        return req;
    }

    private void setupAuth(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(username);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}
