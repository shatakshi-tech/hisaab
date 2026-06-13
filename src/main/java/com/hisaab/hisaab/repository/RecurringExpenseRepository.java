package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    List<RecurringExpense> findByDayOfMonth(int dayOfMonth);
}