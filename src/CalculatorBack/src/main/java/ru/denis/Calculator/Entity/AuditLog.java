package ru.denis.Calculator.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String username;

    @Column(length = 128)
    private String action;

    @Column(length = 256)
    private String endpoint;

    @Column(length = 10)
    private String method;

    @Column(length = 45)
    private String ipAddress;

    private LocalDateTime timestamp;

    @Column(length = 16)
    private String status;
}
