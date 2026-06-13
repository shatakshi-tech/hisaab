package com.hisaab.hisaab.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(nullable = false)
    private String action; // e.g., "EXPENSE_CREATED", "EXPENSE_UPDATED"

    @Column(nullable = false)
    private String entityType; // e.g., "Expense"

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValue; // JSON string, nullable for creates

    @Column(columnDefinition = "TEXT")
    private String newValue; // JSON string

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
