# Implementation Plan: Loan Repayment Schedule

## Overview

This implementation plan breaks down the Loan Repayment Schedule feature into incremental coding tasks. The feature automatically generates installment-by-installment repayment schedules when loans are approved, tracks payment progress by updating installment statuses, and displays schedule information in the loan details view.

The implementation follows a backend-first approach: create database schema and entities, implement core schedule generation logic, add repayment update logic, expose API endpoints, build frontend components, and finally create comprehensive demo data for testing.

## Tasks

- [ ] 1. Create backend entities and repository
  - [x] 1.1 Create InstallmentStatus enum
    - Create `com.ikimina.model.InstallmentStatus` enum with values: PENDING, PAID, OVERDUE, PARTIALLY_PAID
    - _Requirements: 1.5, 2.4_
  
  - [x] 1.2 Create RepaymentSchedule entity
    - Create `com.ikimina.model.RepaymentSchedule` entity class with JPA annotations
    - Define fields: id (Long), loan (ManyToOne), installmentNumber (Integer), dueDate (LocalDate), installmentAmount (BigDecimal), status (InstallmentStatus), remainingBalance (BigDecimal)
    - Add unique constraint on (loan_id, installment_number)
    - Add index on loan_id column
    - Configure cascade delete when parent loan is deleted
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_
  
  - [-] 1.3 Create ScheduleRepository interface
    - Create `com.ikimina.repository.ScheduleRepository` interface extending JpaRepository
    - Add method: `findByLoanIdOrderByInstallmentNumberAsc(Long loanId)`
    - Add method: `findByLoanIdAndStatusIn(Long loanId, List<InstallmentStatus> statuses)`
    - Add method: `findByStatusAndDueDateBefore(InstallmentStatus status, LocalDate date)`
    - Add method: `deleteByLoanId(Long loanId)`
    - Add method: `countByLoanId(Long loanId)`
    - Add custom query method: `countByLoanIdAndStatus` using @Query annotation
    - _Requirements: 2.6, 2.7, 2.8, 3.1_

- [ ] 2. Implement schedule generation logic
  - [x] 2.1 Create ScheduleSummary DTO
    - Create `com.ikimina.dto.ScheduleSummary` class
    - Define fields: loanId, totalInstallments, paidInstallments, overdueInstallments, pendingInstallments, partiallyPaidInstallments, totalPaid, totalPending
    - Add constructors, getters, and setters
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [-] 2.2 Create ScheduleService with generateSchedule method
    - Create `com.ikimina.service.ScheduleService` class with @Service and @Transactional annotations
    - Inject ScheduleRepository and LoanRepository dependencies
    - Implement `generateSchedule(Loan loan)` method that:
      - Validates loan has ACTIVE status and approval date
      - Creates N installment records where N = loan.repaymentMonths
      - Calculates due date as approvalDate + installmentNumber months
      - Sets installment amount to loan.monthlyInstallment
      - Calculates remaining balance descending from totalPayable
      - Sets all statuses to PENDING
      - Persists all records via ScheduleRepository
      - Logs errors but does not throw exceptions
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_
  
  - [ ] 2.3 Write unit tests for generateSchedule
    - Test schedule generation for 3-month loan
    - Test schedule generation for 12-month loan
    - Test installment amounts sum to totalPayable (within rounding tolerance)
    - Test due dates are correctly calculated
    - Test remaining balances decrease correctly
    - Test all installments start with PENDING status
    - Test error handling for missing approval date
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 3. Integrate schedule generation with loan approval
  - [~] 3.1 Modify LoanService.approveLoan to trigger schedule generation
    - Update `com.ikimina.service.LoanService.approveLoan()` method
    - After setting loan status to ACTIVE and saving, call `scheduleService.generateSchedule(loan)`
    - Wrap schedule generation in try-catch block to prevent approval failure
    - Log any schedule generation errors
    - _Requirements: 1.1, 1.6, 1.7_
  
  - [~] 3.2 Write integration test for loan approval with schedule generation
    - Test that approving a loan creates schedule records
    - Test that loan approval succeeds even if schedule generation fails
    - Test cascade delete removes schedules when loan is deleted
    - _Requirements: 1.1, 1.6, 1.7_

- [~] 4. Checkpoint - Verify schedule generation works
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement schedule update logic for repayments
  - [~] 5.1 Implement ScheduleService.updateScheduleFromRepayment method
    - Implement `updateScheduleFromRepayment(Loan loan, BigDecimal repaymentAmount)` method in ScheduleService
    - Retrieve unpaid installments (PENDING, OVERDUE, PARTIALLY_PAID) ordered by dueDate ASC
    - Allocate repayment amount sequentially:
      - If amount >= installment amount: mark PAID, carry excess to next
      - If amount < installment amount: mark PARTIALLY_PAID
    - Update remainingBalance for affected installments
    - Persist all changes via ScheduleRepository
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_
  
  - [~] 5.2 Write unit tests for updateScheduleFromRepayment
    - Test full payment of single installment marks it PAID
    - Test partial payment marks installment PARTIALLY_PAID
    - Test payment exceeding installment applies excess to next installment
    - Test payment fully covering multiple installments updates all
    - Test remaining balances update correctly
    - Test overdue installments can be marked PAID when paid
    - _Requirements: 5.3, 5.4, 5.5, 5.6_
  
  - [~] 5.3 Modify LoanService.recordRepayment to trigger schedule update
    - Update `com.ikimina.service.LoanService.recordRepayment()` method
    - After saving repayment, call `scheduleService.updateScheduleFromRepayment(loan, paymentAmount)`
    - Wrap schedule update in try-catch block to prevent repayment failure
    - Log any schedule update errors
    - _Requirements: 5.1, 5.7_
  
  - [~] 5.4 Write integration test for repayment with schedule update
    - Test that recording repayment updates installment statuses
    - Test that repayment succeeds even if schedule update fails
    - _Requirements: 5.1, 5.7_

- [ ] 6. Implement overdue detection logic
  - [~] 6.1 Implement ScheduleService.markOverdueInstallments method
    - Implement `markOverdueInstallments()` method in ScheduleService
    - Find all PENDING installments where dueDate < current date using repository
    - Update status to OVERDUE for each found installment
    - Return count of updated installments
    - _Requirements: 6.1, 6.2, 6.4, 6.5_
  
  - [~] 6.2 Create ScheduledJobService with daily cron job
    - Create `com.ikimina.service.ScheduledJobService` class with @Service annotation
    - Enable scheduling with @EnableScheduling on main application class
    - Inject ScheduleService dependency
    - Create method annotated with @Scheduled(cron = "0 0 0 * * *") for daily execution at midnight
    - Call `scheduleService.markOverdueInstallments()` and log count of updated installments
    - _Requirements: 6.3, 6.4_
  
  - [~] 6.3 Write unit tests for markOverdueInstallments
    - Test PENDING installments past due date are marked OVERDUE
    - Test future PENDING installments remain PENDING
    - Test PAID installments are not affected
    - Test count of updated installments is returned correctly
    - _Requirements: 6.1, 6.2, 6.4_

- [ ] 7. Implement schedule retrieval and validation logic
  - [~] 7.1 Implement ScheduleService.getScheduleByLoanId method
    - Implement `getScheduleByLoanId(Long loanId)` method in ScheduleService
    - Validate loan exists using LoanRepository
    - Retrieve installments ordered by installment number using ScheduleRepository
    - Return empty list if no installments exist
    - Throw exception with appropriate message if loan not found
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [~] 7.2 Implement ScheduleService.getScheduleSummary method
    - Implement `getScheduleSummary(Long loanId)` method in ScheduleService
    - Use repository count methods to get installment counts by status
    - Calculate total paid and total pending amounts
    - Build and return ScheduleSummary DTO
    - Return zero counts if no installments exist
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_
  
  - [~] 7.3 Implement ScheduleService.validateSchedule method
    - Implement `validateSchedule(Long loanId)` method in ScheduleService
    - Verify installment count equals loan repayment months
    - Verify sum of installment amounts equals loan total payable (within 1 RWF tolerance)
    - Verify each installment has unique number between 1 and repayment months
    - Log warning with loan ID and details for any validation failures
    - Return validation result object with success flag and error details
    - Continue normal operation even if validation fails
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [~] 7.4 Implement ScheduleService.regenerateSchedule method
    - Implement `regenerateSchedule(Long loanId)` method in ScheduleService
    - Validate loan exists and has ACTIVE status
    - Delete existing schedule records using `scheduleRepository.deleteByLoanId()`
    - Call `generateSchedule()` to create new schedule
    - Return newly generated schedule
    - _Requirements: 10.2_
  
  - [~] 7.5 Write unit tests for schedule retrieval and validation
    - Test getScheduleByLoanId returns schedule ordered correctly
    - Test getScheduleByLoanId returns empty list for loan without schedule
    - Test getScheduleByLoanId throws exception for non-existent loan
    - Test getScheduleSummary returns correct counts
    - Test validateSchedule detects inconsistencies
    - Test regenerateSchedule deletes and recreates schedule
    - _Requirements: 3.1, 3.2, 3.5, 8.6, 9.1, 9.2, 9.3_

- [~] 8. Checkpoint - Verify schedule services work correctly
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Add REST API endpoints for schedule operations
  - [~] 9.1 Add schedule endpoints to LoanController
    - Add GET endpoint `/api/loans/{id}/schedule` that calls `scheduleService.getScheduleByLoanId()`
    - Add GET endpoint `/api/loans/{id}/schedule/summary` that calls `scheduleService.getScheduleSummary()`
    - Add POST endpoint `/api/loans/{id}/schedule/regenerate` that calls `scheduleService.regenerateSchedule()`
    - Add proper error handling to return 404 for non-existent loans
    - Add proper error handling to return 400 for invalid inputs
    - Add proper error handling to return 403 for unauthorized access
    - Set proper JSON content type headers
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_
  
  - [~] 9.2 Write API integration tests for schedule endpoints
    - Test GET /api/loans/{id}/schedule returns schedule correctly
    - Test GET /api/loans/{id}/schedule returns 404 for non-existent loan
    - Test GET /api/loans/{id}/schedule returns empty array for loan without schedule
    - Test GET /api/loans/{id}/schedule/summary returns correct counts
    - Test POST /api/loans/{id}/schedule/regenerate recreates schedule
    - Test endpoints return proper error codes for error conditions
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 10. Implement frontend type definitions and API client
  - [~] 10.1 Add schedule types to TypeScript definitions
    - Add `InstallmentStatus` type union in `frontend/src/types/index.ts`
    - Add `RepaymentSchedule` interface with fields: id, installmentNumber, dueDate, installmentAmount, status, remainingBalance
    - Add `ScheduleSummary` interface with fields: loanId, totalInstallments, paidInstallments, overdueInstallments, pendingInstallments, partiallyPaidInstallments, totalPaid, totalPending
    - Add optional `schedule` and `scheduleSummary` fields to existing `Loan` interface
    - _Requirements: 4.2, 4.3, 4.5, 8.5_
  
  - [~] 10.2 Add schedule API methods to api service
    - Add `loanSchedule(loanId: number)` method in `frontend/src/services/api.ts` calling GET `/api/loans/${loanId}/schedule`
    - Add `loanScheduleSummary(loanId: number)` method calling GET `/api/loans/${loanId}/schedule/summary`
    - Add `regenerateLoanSchedule(loanId: number)` method calling POST `/api/loans/${loanId}/schedule/regenerate`
    - Use existing request helper with proper typing
    - _Requirements: 10.1, 10.2, 10.3_

- [ ] 11. Create frontend schedule display components
  - [~] 11.1 Create InstallmentStatusBadge component
    - Create `frontend/src/components/InstallmentStatusBadge.tsx`
    - Accept `status: InstallmentStatus` prop
    - Render badge with appropriate color: PAID (green), PENDING (yellow), OVERDUE (red), PARTIALLY_PAID (orange)
    - Use Tailwind CSS classes for styling
    - _Requirements: 4.5_
  
  - [~] 11.2 Create ScheduleTable component
    - Create `frontend/src/components/ScheduleTable.tsx`
    - Accept `schedule: RepaymentSchedule[]` and `isLoading: boolean` props
    - Display table with columns: Installment #, Due Date, Amount, Status, Remaining Balance
    - Format dates as DD-MM-YYYY using existing format utilities
    - Format amounts as RWF currency with 2 decimal places
    - Render status using InstallmentStatusBadge component
    - Display loading state when isLoading is true
    - Display empty state when schedule is empty
    - Order installments by installment number ascending
    - _Requirements: 4.2, 4.3, 4.4, 4.5, 4.7_
  
  - [~] 11.3 Create ScheduleSummaryPanel component
    - Create `frontend/src/components/ScheduleSummaryPanel.tsx`
    - Accept `summary: ScheduleSummary` prop
    - Display grid of 4 MetricCard components showing: Total Installments, Paid (green), Overdue (red), Pending (yellow)
    - Use existing MetricCard component for consistent styling
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [~] 11.4 Write unit tests for schedule components
    - Test InstallmentStatusBadge renders correct colors for each status
    - Test ScheduleTable renders data correctly
    - Test ScheduleTable displays empty state
    - Test ScheduleTable displays loading state
    - Test ScheduleSummaryPanel displays all metrics
    - _Requirements: 4.2, 4.5_

- [ ] 12. Integrate schedule into Loan Details Page
  - [~] 12.1 Enhance Loan Details Page with schedule display
    - Update `frontend/src/pages/MemberProfilePage.tsx` (or create separate loan detail view if needed)
    - Add state for `schedule: RepaymentSchedule[]` and `summary: ScheduleSummary`
    - Fetch schedule and summary data using api methods when loan is loaded
    - Add "Payment Progress" panel displaying ScheduleSummaryPanel component
    - Add "Repayment Schedule" panel displaying ScheduleTable component
    - Handle loading and error states appropriately
    - Display "No schedule available" message when schedule is empty
    - _Requirements: 4.1, 4.2, 4.6, 4.7_
  
  - [~] 12.2 Write integration tests for enhanced loan details page
    - Test page fetches and displays schedule correctly
    - Test page fetches and displays summary correctly
    - Test page handles missing schedule gracefully
    - Test page handles loading errors appropriately
    - _Requirements: 4.1, 4.6_

- [ ] 13. Create comprehensive demo data seeder
  - [~] 13.1 Create or enhance DataSeeder component
    - Create or update `com.ikimina.config.DataSeeder` class with @Component annotation
    - Implement CommandLineRunner interface with run() method
    - Check if admin user exists; if exists, skip seeding to prevent duplicates
    - Create admin user with username "admin" and password "admin123"
    - Create exactly 4 demo members with varying join dates (1-6 months before current date)
    - Assign random total savings between 50,000 and 500,000 RWF to each member
    - Create at least 2 loans with ACTIVE status for eligible members with varying repayment terms (3-12 months)
    - For each ACTIVE loan, trigger schedule generation via LoanService
    - Create at least 2 repayments for one of the demo loans to demonstrate status updates
    - Log all created entities (user, members, loans, repayments) for verification
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_
  
  - [~] 13.2 Write unit tests for demo data seeder
    - Test seeder creates correct number of members
    - Test seeder creates correct number of loans with schedules
    - Test seeder creates repayments that update schedules
    - Test seeder skips execution when admin exists
    - _Requirements: 7.2, 7.4, 7.5, 7.6, 7.7_

- [~] 14. Final checkpoint - Verify complete feature functionality
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- All implementation tasks reference specific requirements for traceability
- The design explicitly states property-based testing is not appropriate, so only unit and integration tests are included
- Backend uses Java/Spring Boot with JPA; frontend uses TypeScript/React
- Schedule generation and updates are designed to not block core loan operations (approval, repayment)
- Error handling ensures loan operations succeed even if schedule operations fail
- Checkpoints at natural breaking points allow for incremental validation

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3", "2.2", "2.3"] },
    { "id": 3, "tasks": ["3.1", "3.2"] },
    { "id": 4, "tasks": ["5.1", "5.2"] },
    { "id": 5, "tasks": ["5.3", "5.4", "6.1", "6.3"] },
    { "id": 6, "tasks": ["6.2", "7.1", "7.2", "7.3", "7.4", "7.5"] },
    { "id": 7, "tasks": ["9.1", "9.2", "10.1"] },
    { "id": 8, "tasks": ["10.2"] },
    { "id": 9, "tasks": ["11.1"] },
    { "id": 10, "tasks": ["11.2", "11.3", "11.4"] },
    { "id": 11, "tasks": ["12.1", "12.2"] },
    { "id": 12, "tasks": ["13.1", "13.2"] }
  ]
}
```
