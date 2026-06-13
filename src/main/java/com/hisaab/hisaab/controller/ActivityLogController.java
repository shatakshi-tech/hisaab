package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.entity.ActivityLog;
import com.hisaab.hisaab.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @GetMapping("/{entityType}/{entityId}")
    public List<ActivityLog> getLogs(@PathVariable String entityType, @PathVariable Long entityId) {
        return activityLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
