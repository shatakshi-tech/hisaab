package com.hisaab.hisaab.repository;

import com.hisaab.hisaab.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GrpMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(Long userId);
}