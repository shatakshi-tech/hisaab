package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    List<ExpenseShare> findByExpenseId(Long expenseId);
    List<ExpenseShare> findByUserId(Long userId);
}