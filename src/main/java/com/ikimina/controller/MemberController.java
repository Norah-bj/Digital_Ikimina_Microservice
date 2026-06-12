package com.ikimina.controller;

import com.ikimina.dto.MemberRequest;
import com.ikimina.dto.MemberSummaryResponse;
import com.ikimina.model.Member;
import com.ikimina.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody MemberRequest request) {
        return new ResponseEntity<>(memberService.createMember(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id " + id)));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<MemberSummaryResponse> getMemberSummary(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberSummary(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Member> activateMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.activateMember(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Member> deactivateMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.deactivateMember(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberHard(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
