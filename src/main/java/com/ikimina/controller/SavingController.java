package com.ikimina.controller;

import com.ikimina.dto.SavingRequest;
import com.ikimina.model.Saving;
import com.ikimina.service.SavingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/savings")
public class SavingController {

    private final SavingService savingService;

    public SavingController(SavingService savingService) {
        this.savingService = savingService;
    }

    @PostMapping
    public ResponseEntity<Saving> recordSaving(@Valid @RequestBody SavingRequest request) {
        return new ResponseEntity<>(savingService.recordSaving(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Saving>> getAllSavings() {
        return ResponseEntity.ok(savingService.getAllSavings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Saving> getSavingById(@PathVariable Long id) {
        return ResponseEntity.ok(savingService.getSavingById(id));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Saving>> getSavingsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(savingService.getSavingsByMemberId(memberId));
    }

    @GetMapping("/member/{memberId}/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalSavingsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(Map.of("totalSavings", savingService.getTotalSavingsByMemberId(memberId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaving(@PathVariable Long id) {
        savingService.deleteSaving(id);
        return ResponseEntity.noContent().build();
    }
}
