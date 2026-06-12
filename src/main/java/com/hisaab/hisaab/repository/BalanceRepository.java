package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    List<Balance> findByGroupId(Long groupId);

    Optional<Balance> findByGroupIdAndOwedByIdAndOwedToId(
            Long groupId, Long owedById, Long owedToId
    );
}