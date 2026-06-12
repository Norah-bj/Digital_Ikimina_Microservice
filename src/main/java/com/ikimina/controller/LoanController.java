package com.ikimina.controller;

import com.ikimina.dto.LoanDecisionRequest;
import com.ikimina.dto.LoanRequest;
import com.ikimina.dto.RepaymentRequest;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Repayment;
import com.ikimina.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<Loan> requestLoan(@Valid @RequestBody LoanRequest request) {
        return new ResponseEntity<>(loanService.requestLoan(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans(@RequestParam(required = false) LoanStatus status) {
        if (status != null) {
            return ResponseEntity.ok(loanService.getLoansByStatus(status));
        }
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Loan>> getLoansByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(loanService.getLoansByMemberId(memberId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id, @RequestBody(required = false) LoanDecisionRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(loanService.rejectLoan(id, reason));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Loan> cancelLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.cancelLoan(id));
    }

    @PostMapping("/{id}/repayments")
    public ResponseEntity<Repayment> recordRepayment(@PathVariable Long id, @Valid @RequestBody RepaymentRequest request) {
        return new ResponseEntity<>(loanService.recordRepayment(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/repayments")
    public ResponseEntity<List<Repayment>> getRepaymentsByLoanId(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getRepaymentsByLoanId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
