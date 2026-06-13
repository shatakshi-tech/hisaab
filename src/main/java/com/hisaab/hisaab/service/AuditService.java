package com.hisaab.hisaab.service;

import tools.jackson.databind.ObjectMapper;
import com.hisaab.hisaab.entity.ActivityLog;
import com.hisaab.hisaab.entity.User;
import com.hisaab.hisaab.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(User performedBy, String action, String entityType, Long entityId,
                    Object oldValue, Object newValue) {
        try {
            ActivityLog logEntry = new ActivityLog();
            logEntry.setPerformedBy(performedBy);
            logEntry.setAction(action);
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            logEntry.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            activityLogRepository.save(logEntry);
        } catch (Exception e) {
            // Don't let audit logging failures break the main transaction
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }
}
