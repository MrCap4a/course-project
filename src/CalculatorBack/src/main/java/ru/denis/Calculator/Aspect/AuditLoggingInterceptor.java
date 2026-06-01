package ru.denis.Calculator.Aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.denis.Calculator.Entity.AuditLog;
import ru.denis.Calculator.Foundation.AuditLogRepository;

import java.time.LocalDateTime;

@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "audit_start";
    private static final String ATTR_ACTION = "audit_action";

    private final AuditLogRepository auditLogRepository;

    public AuditLoggingInterceptor(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method) {
            request.setAttribute(ATTR_START, System.currentTimeMillis());
            request.setAttribute(ATTR_ACTION, method.getShortLogMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (request.getAttribute(ATTR_ACTION) == null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        AuditLog entry = new AuditLog();
        entry.setUsername(username);
        entry.setAction((String) request.getAttribute(ATTR_ACTION));
        entry.setEndpoint(request.getRequestURI());
        entry.setMethod(request.getMethod());
        entry.setIpAddress(request.getRemoteAddr());
        entry.setTimestamp(LocalDateTime.now());
        entry.setStatus(ex == null && response.getStatus() < 500 ? "SUCCESS" : "ERROR");
        auditLogRepository.save(entry);
    }
}
