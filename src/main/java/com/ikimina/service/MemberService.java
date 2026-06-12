package com.ikimina.service;

import com.ikimina.dto.MemberRequest;
import com.ikimina.dto.MemberSummaryResponse;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Member;
import com.ikimina.model.MemberRole;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.SavingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final SavingRepository savingRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository, SavingRepository savingRepository,
                         LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.savingRepository = savingRepository;
        this.loanRepository = loanRepository;
    }

    public Member createMember(MemberRequest request) {
        validateUniqueMember(request.getEmail(), request.getNationalId(), null);

        Member member = new Member();
        applyRequest(member, request);
        member.setIsActive(true);
        member.setJoinDate(request.getJoinDate() == null ? LocalDate.now() : request.getJoinDate());
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(requireId(id, "Member ID is required"));
    }

    public Member updateMember(Long id, MemberRequest request) {
        Member member = findMember(id);
        validateUniqueMember(request.getEmail(), request.getNationalId(), id);
        applyRequest(member, request);
        if (request.getJoinDate() != null) {
            member.setJoinDate(request.getJoinDate());
        }
        return memberRepository.save(member);
    }

    public Member activateMember(Long id) {
        Member member = findMember(id);
        member.setIsActive(true);
        return memberRepository.save(member);
    }

    public Member deactivateMember(Long id) {
        Member member = findMember(id);
        member.setIsActive(false);
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        Long safeId = requireId(id, "Member ID is required");
        if (!memberRepository.existsById(safeId)) {
            throw new RuntimeException("Member not found with id " + safeId);
        }
        if (!savingRepository.findByMemberId(safeId).isEmpty()
                || !loanRepository.findByMemberId(safeId).isEmpty()
                || !loanRepository.findByGuarantorId(safeId).isEmpty()) {
            throw new IllegalArgumentException("Member has financial records. Deactivate the member instead of deleting.");
        }
        memberRepository.deleteById(safeId);
    }

    @Transactional(readOnly = true)
    public MemberSummaryResponse getMemberSummary(Long id) {
        Long safeId = requireId(id, "Member ID is required");
        Member member = findMember(safeId);
        BigDecimal totalSavings = zeroIfNull(savingRepository.sumByMemberId(safeId));
        BigDecimal activeLoanBalance = loanRepository
                .findByMemberIdAndStatusIn(safeId, List.of(LoanStatus.ACTIVE, LoanStatus.DEFAULTED))
                .stream()
                .map(Loan::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyContribution = member.getMonthlySalary()
                .multiply(member.getContributionPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new MemberSummaryResponse(
                member.getId(),
                member.getFullName(),
                totalSavings,
                totalSavings,
                activeLoanBalance,
                monthlyContribution,
                member.getSavings().size(),
                member.getLoans().size()
        );
    }

    public @NonNull Member findMember(Long id) {
        Long safeId = requireId(id, "Member ID is required");
        Member member = memberRepository.findById(safeId).orElse(null);
        if (member == null) {
            throw new RuntimeException("Member not found with id " + safeId);
        }
        return member;
    }

    private void applyRequest(Member member, MemberRequest request) {
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setNationalId(request.getNationalId());
        member.setMonthlySalary(defaultMoney(request.getMonthlySalary()));
        member.setContributionPercentage(defaultMoney(request.getContributionPercentage()));
        member.setRole(request.getRole() == null ? MemberRole.MEMBER : request.getRole());
    }

    private void validateUniqueMember(String email, String nationalId, Long currentMemberId) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            if (!member.getId().equals(currentMemberId)) {
                throw new IllegalArgumentException("Email is already used by another member");
            }
        });

        if (nationalId != null && !nationalId.isBlank()) {
            memberRepository.findByNationalId(nationalId).ifPresent(member -> {
                if (!member.getId().equals(currentMemberId)) {
                    throw new IllegalArgumentException("National ID is already used by another member");
                }
            });
        }
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private @NonNull Long requireId(Long id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }
}
