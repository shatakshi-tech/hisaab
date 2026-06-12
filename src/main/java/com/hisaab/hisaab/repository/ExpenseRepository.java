package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupId(Long groupId);
}