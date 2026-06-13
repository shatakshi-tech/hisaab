package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.CreateGroupRequest;
import com.hisaab.hisaab.dto.GroupSummaryDto;
import com.hisaab.hisaab.entity.Group;
import com.hisaab.hisaab.entity.GroupMember;
import com.hisaab.hisaab.entity.User;
import com.hisaab.hisaab.repository.GrpMemberRepository;
import com.hisaab.hisaab.repository.GroupRepository;
import com.hisaab.hisaab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GrpMemberRepository grpMemberRepository;


    @GetMapping("/my-groups/{userId}")
    public List<GroupSummaryDto> getMyGroups(@PathVariable Long userId) {
        return grpMemberRepository.findByUserId(userId)
                .stream()
                .map(gm -> new GroupSummaryDto(gm.getGroup().getId(), gm.getGroup().getName()))
                .collect(Collectors.toList());
    }

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
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyMember = grpMemberRepository.findByGroupId(groupId)
                .stream().anyMatch(gm -> gm.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is already a member"));
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        return ResponseEntity.ok(grpMemberRepository.save(member));
    }

    @PostMapping("/{groupId}/members/by-email")
    public ResponseEntity<?> addMemberByEmail(@PathVariable Long groupId, @RequestBody Map<String, String> body) {
        String email = body.get("email");

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));

        boolean alreadyMember = grpMemberRepository.findByGroupId(groupId)
                .stream().anyMatch(gm -> gm.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is already a member"));
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        grpMemberRepository.save(member);

        return ResponseEntity.ok(Map.of("message", "Added " + user.getName() + " to group"));
    }
}