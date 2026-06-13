package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.GroupSummaryResponse;
import com.hisaab.hisaab.service.GroupSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupSummaryController {

    @Autowired
    private GroupSummaryService groupSummaryService;

    @GetMapping("/{groupId}/summary")
    public GroupSummaryResponse getSummary(@PathVariable Long groupId) {
        return groupSummaryService.getSummary(groupId);
    }
}
