package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
