package com.ikimina.service;

import com.ikimina.dto.SavingRequest;
import com.ikimina.model.Member;
import com.ikimina.model.Saving;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.SavingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SavingService {

    private static final BigDecimal MINIMUM_MONTHLY_SAVING = BigDecimal.valueOf(5000);

    private final SavingRepository savingRepository;
    private final MemberRepository memberRepository;

    public SavingService(SavingRepository savingRepository, MemberRepository memberRepository) {
        this.savingRepository = savingRepository;
        this.memberRepository = memberRepository;
    }

    public Saving recordSaving(SavingRequest request) {
        if (request.getAmount().compareTo(MINIMUM_MONTHLY_SAVING) < 0) {
            throw new IllegalArgumentException("Saving amount must be at least 5000 RWF");
        }

        Long memberId = requireId(request.getMemberId(), "Member ID is required");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id " + memberId));

        if (!Boolean.TRUE.equals(member.getIsActive())) {
            throw new IllegalArgumentException("Member must be active to create a saving");
        }

        savingRepository.findByMemberIdAndSavingMonth(requireId(member.getId(), "Member ID is missing"), request.getSavingMonth()).ifPresent(existing -> {
            throw new IllegalArgumentException("This member already has a saving recorded for " + request.getSavingMonth());
        });

        Saving saving = new Saving();
        saving.setMember(member);
        saving.setAmount(request.getAmount());
        saving.setSavingMonth(request.getSavingMonth());
        return savingRepository.save(saving);
    }

    @Transactional(readOnly = true)
    public List<Saving> getAllSavings() {
        return savingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Saving getSavingById(Long id) {
        Long safeId = requireId(id, "Saving ID is required");
        return savingRepository.findById(safeId)
                .orElseThrow(() -> new RuntimeException("Saving not found with id " + safeId));
    }

    @Transactional(readOnly = true)
    public List<Saving> getSavingsByMemberId(Long memberId) {
        return savingRepository.findByMemberId(requireId(memberId, "Member ID is required"));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSavingsByMemberId(Long memberId) {
        Long safeMemberId = requireId(memberId, "Member ID is required");
        if (!memberRepository.existsById(safeMemberId)) {
            throw new RuntimeException("Member not found with id " + safeMemberId);
        }
        BigDecimal total = savingRepository.sumByMemberId(safeMemberId);
        return total == null ? BigDecimal.ZERO : total;
    }

    public void deleteSaving(Long id) {
        Long safeId = requireId(id, "Saving ID is required");
        if (!savingRepository.existsById(safeId)) {
            throw new RuntimeException("Saving not found with id " + safeId);
        }
        savingRepository.deleteById(safeId);
    }

    private @NonNull Long requireId(Long id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }
}
