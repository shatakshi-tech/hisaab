package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}