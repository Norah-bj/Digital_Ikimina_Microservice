package com.ikimina.service;

import com.ikimina.dto.MemberRequest;
import com.ikimina.model.Member;
import com.ikimina.model.MemberRole;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.SavingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SavingRepository savingRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberRequest request;

    @BeforeEach
    void setUp() {
        request = new MemberRequest();
        request.setFullName("Test User");
        request.setEmail("test@test.com");
        request.setPhone("0780000000");
        request.setNationalId("1199080000000000");
        request.setMonthlySalary(new BigDecimal("100000"));
        request.setContributionPercentage(new BigDecimal("10"));
        request.setRole(MemberRole.MEMBER);
    }

    @Test
    void createMember_Success() {
        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByNationalId(request.getNationalId())).thenReturn(Optional.empty());
        
        Member savedMember = new Member();
        savedMember.setId(1L);
        savedMember.setFullName(request.getFullName());
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        Member result = memberService.createMember(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getFullName());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void createMember_DuplicateEmail_ThrowsException() {
        Member existingMember = new Member();
        existingMember.setId(2L);
        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingMember));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> memberService.createMember(request));
        assertEquals("Email is already used by another member", ex.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }
}
