package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.denis.Calculator.Entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
