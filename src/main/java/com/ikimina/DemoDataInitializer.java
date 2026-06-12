package com.ikimina;

import com.ikimina.dto.LoanRequest;
import com.ikimina.model.Member;
import com.ikimina.model.MemberRole;
import com.ikimina.model.Saving;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.SavingRepository;
import com.ikimina.service.LoanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final SavingRepository savingRepository;
    private final LoanService loanService;

    public DemoDataInitializer(MemberRepository memberRepository, 
                               SavingRepository savingRepository, 
                               LoanService loanService) {
        this.memberRepository = memberRepository;
        this.savingRepository = savingRepository;
        this.loanService = loanService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail("admin@ikimina.com").isEmpty()) {
            createAdmin();
        }

        if (memberRepository.findByEmail("alice@ikimina.com").isEmpty()) {
            Member alice = createMember("Alice Uwimana", "alice@ikimina.com", "0781000001", "1199080000000001", 4, new BigDecimal("250000"));
            Member jeanBosco = createMember("Jean Bosco", "jean.bosco@ikimina.com", "0781000002", "1199080000000002", 5, new BigDecimal("500000"));
            Member diane = createMember("Diane Mukamana", "diane@ikimina.com", "0781000003", "1199080000000003", 6, new BigDecimal("350000"));
            Member patrick = createMember("Patrick Ndayisaba", "patrick@ikimina.com", "0781000004", "1199080000000004", 10, new BigDecimal("50000"));

            // Alice wants a loan of 200,000 for 6 months (no guarantor needed as it's < savings)
            LoanRequest aliceLoanReq = new LoanRequest();
            aliceLoanReq.setMemberId(alice.getId());
            aliceLoanReq.setAmount(new BigDecimal("200000"));
            aliceLoanReq.setRepaymentMonths(6);
            aliceLoanReq.setPurpose("Business Expansion");
            var aliceLoan = loanService.requestLoan(aliceLoanReq);
            aliceLoan = loanService.approveLoan(aliceLoan.getId());

            // Alice pays first 2 installments
            com.ikimina.dto.RepaymentRequest aliceRepayment1 = new com.ikimina.dto.RepaymentRequest();
            aliceRepayment1.setAmount(aliceLoan.getMonthlyInstallment());
            aliceRepayment1.setPaymentDate(LocalDate.now().minusMonths(1));
            loanService.recordRepayment(aliceLoan.getId(), aliceRepayment1);

            com.ikimina.dto.RepaymentRequest aliceRepayment2 = new com.ikimina.dto.RepaymentRequest();
            aliceRepayment2.setAmount(aliceLoan.getMonthlyInstallment());
            aliceRepayment2.setPaymentDate(LocalDate.now());
            loanService.recordRepayment(aliceLoan.getId(), aliceRepayment2);

            // Jean Bosco wants a loan of 400,000 for 10 months (no guarantor needed as it's < 500k savings)
            LoanRequest jeanLoanReq = new LoanRequest();
            jeanLoanReq.setMemberId(jeanBosco.getId());
            jeanLoanReq.setAmount(new BigDecimal("400000"));
            jeanLoanReq.setRepaymentMonths(10);
            jeanLoanReq.setPurpose("Home Renovation");
            var jeanLoan = loanService.requestLoan(jeanLoanReq);
            jeanLoan = loanService.approveLoan(jeanLoan.getId());

            // Jean Bosco pays 1 installment
            com.ikimina.dto.RepaymentRequest jeanRepayment1 = new com.ikimina.dto.RepaymentRequest();
            jeanRepayment1.setAmount(jeanLoan.getMonthlyInstallment());
            jeanRepayment1.setPaymentDate(LocalDate.now());
            loanService.recordRepayment(jeanLoan.getId(), jeanRepayment1);
        }
    }

    private void createAdmin() {
        Member admin = new Member();
        admin.setFullName("System Admin");
        admin.setEmail("admin@ikimina.com");
        admin.setNationalId("0000000000000000");
        admin.setRole(MemberRole.SUPER_ADMIN);
        admin.setMonthlySalary(BigDecimal.ZERO);
        admin.setContributionPercentage(BigDecimal.ZERO);
        memberRepository.save(admin);
    }

    private Member createMember(String fullName, String email, String phone, String nationalId, int membershipMonths, BigDecimal totalSavings) {
        Member member = new Member();
        member.setFullName(fullName);
        member.setEmail(email);
        member.setPhone(phone);
        member.setNationalId(nationalId);
        member.setRole(MemberRole.MEMBER);
        member.setJoinDate(LocalDate.now().minusMonths(membershipMonths));
        member.setMonthlySalary(new BigDecimal("1000000")); // So that 60% of salary rule is not breached easily
        member.setContributionPercentage(new BigDecimal("10"));
        member = memberRepository.save(member);

        if (totalSavings.compareTo(BigDecimal.ZERO) > 0) {
            // Distribute savings across the months
            BigDecimal monthlySaving = totalSavings.divide(BigDecimal.valueOf(membershipMonths), 2, java.math.RoundingMode.HALF_UP);
            for (int i = 0; i < membershipMonths; i++) {
                String month = YearMonth.now().minusMonths(i).toString();
                Saving saving = new Saving(monthlySaving, month, member);
                savingRepository.save(saving);
            }
        }
        return member;
    }
}
