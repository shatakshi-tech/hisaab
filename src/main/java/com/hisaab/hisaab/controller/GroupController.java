package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.CreateGroupRequest;
import com.hisaab.hisaab.entity.Group;
import com.hisaab.hisaab.entity.GroupMember;
import com.hisaab.hisaab.entity.User;
import com.hisaab.hisaab.repository.GrpMemberRepository;
import com.hisaab.hisaab.repository.GroupRepository;
import com.hisaab.hisaab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GrpMemberRepository grpMemberRepository;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedBy(creator);
        Group saved = groupRepository.save(group);

        // Add creator as a group member automatically
        GroupMember member = new GroupMember();
        member.setGroup(saved);
        member.setUser(creator);
        grpMemberRepository.save(member);

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMember> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        return ResponseEntity.ok(grpMemberRepository.save(member));
    }
}