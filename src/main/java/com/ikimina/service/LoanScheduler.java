package com.ikimina.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduler {

    private final LoanService loanService;

    public LoanScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    // Runs every day at 1:00 AM to flag overdue active loans.
    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueLoansAsDefaulted() {
        loanService.markOverdueLoansAsDefaulted();
    }
}
