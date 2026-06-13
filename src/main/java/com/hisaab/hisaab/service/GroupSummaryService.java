package com.hisaab.hisaab.service;

import com.hisaab.hisaab.dto.GroupSummaryResponse;
import com.hisaab.hisaab.entity.Expense;
import com.hisaab.hisaab.entity.Group;
import com.hisaab.hisaab.repository.ExpenseRepository;
import com.hisaab.hisaab.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GroupSummaryService {

    @Autowired private GroupRepository groupRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private SettlementService settlementService;

    @Cacheable(value = "groupSummary", key = "#groupId")
    public GroupSummaryResponse getSummary(Long groupId) {
        System.out.println("CACHE MISS - computing summary for group " + groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        BigDecimal total = expenseRepository.findByGroupId(groupId)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new GroupSummaryResponse(
                group.getId(),
                group.getName(),
                total,
                settlementService.getSettlements(groupId)
        );
    }

    @CacheEvict(value = "groupSummary", key = "#groupId")
    public void evictSummaryCache(Long groupId) {
        System.out.println("CACHE EVICTED for group " + groupId);
    }
}