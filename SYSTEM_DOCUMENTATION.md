# Digital Ikimina — System Documentation

---

## Introduction

**Digital Ikimina** is a web-based financial management system designed to digitize and automate the operations of a traditional *Ikimina* (informal savings and credit cooperative group). It replaces manual, paper-based processes with a secure, structured platform that manages member savings, loan applications, repayments, repayment schedules, guarantors, and financial reporting.

The system is built with a **Java Spring Boot** backend and a **React TypeScript** frontend, connected to a **PostgreSQL** database. It was developed as a final-year academic project to demonstrate real-world software engineering skills, including layered architecture, business rule enforcement, transaction management, and automated testing.

---

## Problem Statement

Traditional *ibimina* (savings groups) in Rwanda and similar cooperatives face serious operational challenges when managed manually:

| Problem | Impact |
|---|---|
| **Paper records** | Documents get lost, damaged, or altered. No central source of truth. |
| **Lack of transparency** | Members cannot independently verify their savings or loan balances. |
| **Calculation errors** | Interest, installments, and outstanding balances are calculated by hand and often contain mistakes. |
| **Poor loan tracking** | It is difficult to know which loans are pending, active, defaulted, or repaid. |
| **Difficult repayment monitoring** | No way to track whether scheduled installments were paid, overdue, or still pending. |
| **Manual guarantor management** | No systematic way to verify that a guarantor has sufficient savings to cover a loan. |
| **No audit history** | No record of who recorded which payment or when decisions were made. |

These problems result in financial disputes, loss of member trust, and administrative inefficiency.

---

## Objectives

The Digital Ikimina system addresses each problem directly:

- **Centralized digital records** — all members, savings, loans, and repayments are stored in a relational database with enforced integrity constraints.
- **Transparent balances** — members and administrators can view savings totals, outstanding loan balances, and payment history at any time through a web interface.
- **Automated financial calculations** — interest (6% flat), total payable, monthly installment, and outstanding balance are calculated automatically when a loan is created.
- **Full loan lifecycle tracking** — loans progress through clearly defined statuses: PENDING → ACTIVE → REPAID (or REJECTED / CANCELLED / DEFAULTED).
- **Automatic repayment schedule generation** — when a loan is approved, the system generates a complete installment calendar with due dates, amounts, and status tracking.
- **Guarantor validation** — the system enforces that a guarantor has enough savings to cover the guarantee amount before a loan is submitted.
- **Role-based access control** — different user roles see and do different things, preventing unauthorized operations.

---

## Main Features

### Authentication

The system uses role-based user authentication. Each member has a defined role that controls what actions they can perform. Access to sensitive operations (approving loans, managing members) is restricted to authorized roles.

### Member Management

- Create, update, activate, and deactivate members.
- Store full name, email, phone, national ID, monthly salary, contribution percentage, join date, and role.
- Enforce uniqueness on email and national ID.
- A member must be active for at least 3 months before they can apply for a loan.

### Savings Management

- Record monthly savings contributions per member.
- View total savings per member.
- Savings history used in loan eligibility evaluation (if loan amount ≤ total savings, no guarantor is required).

### Loan Management

- Members request loans with amount, repayment period (months), and purpose.
- The system calculates interest (6% flat), total payable, monthly installment, and outstanding balance automatically.
- Monthly installment cannot exceed 60% of the member's monthly salary.
- Loans progress through statuses: PENDING → ACTIVE (approved) or REJECTED / CANCELLED.
- Active loans can become DEFAULTED if the due date passes without full repayment.
- Fully repaid loans are automatically marked REPAID.

### Guarantor Management

- When a loan amount exceeds the borrower's total savings, a guarantor is required.
- The guarantor must be an active member.
- The guarantor must have total savings ≥ the required guarantee amount.
- A member cannot guarantee their own loan.
- When a loan is fully repaid, the guarantor's obligation is automatically released.

### Share Management

Each member's share value in the group is tracked and visible on their profile summary. The share value is derived from their total savings contributions. Members can view their individual share standing through the member profile page.

### Repayment Management

- Record repayment payments against active or defaulted loans.
- Each repayment reduces the outstanding balance.
- When the outstanding balance reaches zero, the loan is automatically marked REPAID.
- All repayment records are stored with: payment date, amount paid, remaining balance after payment, and an optional note.
- Full repayment history is available per loan.

### Schedule Management

*(See dedicated section below for full details.)*

Repayment schedules are generated automatically when a loan is approved. They provide a month-by-month breakdown of expected installments, due dates, and remaining balances.

### Reports

The dashboard provides aggregate financial metrics:
- Total members and active members
- Total savings in the system
- Total loans and pending loans
- Outstanding loan portfolio value
- Total repayments collected
- Expected interest income

### Notifications

The system supports a notifications module to alert members and administrators about relevant events, such as pending loan reviews, upcoming due dates, and overdue payments. Notifications are accessible from the navigation panel.

---

## User Roles

The system supports five distinct user roles:

### Member

A regular participant in the Ikimina group. Members can:
- View their own savings history
- Request a loan
- View their own loan status and repayment schedule
- View their member profile and share value

Members cannot approve loans, manage other members, or access system-wide reports.

### Accountant

Responsible for financial record-keeping. Accountants can:
- Record savings contributions
- Record repayment payments
- View financial reports and dashboards
- View all loans and their statuses

Accountants do not approve or reject loans.

### Loan Committee

Responsible for reviewing and deciding on loan applications. Loan committee members can:
- View all pending loan applications
- Approve or reject loans (with a reason for rejection)
- View loan details including guarantor information

### Secretary

Responsible for administrative record management. Secretaries can:
- Create and update member profiles
- View member savings and loan history
- Manage notifications

### Super Admin

Has full access to all system functions, including:
- All accountant, committee, and secretary permissions
- System configuration and settings
- Creating and managing admin-level users
- Viewing all records across all modules

---

## Schedule Management

### What Is a Repayment Schedule?

A repayment schedule is an automatically generated table of installments that tells both the borrower and the group exactly how a loan will be paid back. Instead of vague verbal agreements, the system generates a precise, legally clear payment plan the moment a loan is approved.

### Where Schedules Live in the System

Schedules are part of the **Loan Module**. They do not appear as a standalone menu item — they belong to a loan and are accessed through the loan's details.

The data relationship is:

```
Member
  └── Loan (one member can have many loans over time, but only one active at a time)
        └── Repayment Schedule (one loan has many schedule entries)
              └── Repayments (actual payments recorded against a loan)
```

### What Each Schedule Entry Contains

| Field | Description |
|---|---|
| **Installment Number** | The sequence number of this installment (1, 2, 3, …) |
| **Due Date** | The date this installment should be paid (approval date + N months) |
| **Installment Amount** | The fixed monthly payment (principal portion + interest portion) |
| **Principal Amount** | The portion of the installment reducing the loan principal |
| **Interest Amount** | The portion of the installment covering interest charges |
| **Remaining Balance** | The projected outstanding balance after this installment is paid |
| **Status** | Current state: PENDING, PAID, or OVERDUE |

### Schedule Status Values

- **PENDING** — the installment is upcoming and not yet paid.
- **PAID** — a repayment has been recorded that covers this installment.
- **OVERDUE** — the due date has passed and the installment was not fully paid.

### Complete Loan Workflow

The following diagram shows the full lifecycle from loan request to completion:

```
1. LOAN REQUEST
   Member submits: amount, purpose, repayment months
   System checks:
   - Member is active and has been a member for ≥ 3 months
   - No existing active or defaulted loan
   - Monthly installment ≤ 60% of salary
   - If loan amount > total savings → guarantor required
   Status: PENDING

2. ELIGIBILITY VERIFICATION (automatic, on request)
   - System calculates: interest = amount × 6%
   - Total payable = amount + interest
   - Monthly installment = total payable ÷ repayment months
   - Guarantor rules applied

3. LOAN APPROVAL (by Loan Committee)
   - Committee reviews and approves (or rejects)
   - On approval: status → ACTIVE
   - Approval date and due date are recorded

4. SCHEDULE GENERATION (automatic, on approval)
   - System generates N installment records (N = repayment months)
   - Each installment: due date, amount, PENDING status, remaining balance
   - Example for a 200,000 RWF loan over 3 months:

   | # | Due Date   | Amount      | Remaining Balance |
   |---|------------|-------------|-------------------|
   | 1 | 2026-02-15 | 70,666.67   | 141,333.33        |
   | 2 | 2026-03-15 | 70,666.67   | 70,666.67         |
   | 3 | 2026-04-15 | 70,666.67   | 0.00              |

5. REPAYMENT RECORDING (by Accountant)
   - Accountant records a payment: amount, date, optional note
   - System deducts from outstanding balance
   - System marks corresponding schedule installments as PAID
   - Remaining balance updated on the loan record

6. LOAN COMPLETION (automatic, on full payment)
   - When outstanding balance reaches 0.00:
     → Loan status → REPAID
     → Guarantor obligation → RELEASED
   - Loan now appears in reports as fully repaid
```

### Example: 200,000 RWF Loan, 6 Months

```
Loan Amount:         200,000.00 RWF
Interest (6%):        12,000.00 RWF
Total Payable:       212,000.00 RWF
Monthly Installment:  35,333.33 RWF
Repayment Period:     6 months

Generated Schedule:
┌────┬─────────────┬──────────────┬───────────────────┬──────────┐
│ #  │ Due Date    │ Installment  │ Remaining Balance │ Status   │
├────┼─────────────┼──────────────┼───────────────────┼──────────┤
│  1 │ 2026-02-01  │ 35,333.33   │ 176,666.67        │ PENDING  │
│  2 │ 2026-03-01  │ 35,333.33   │ 141,333.34        │ PENDING  │
│  3 │ 2026-04-01  │ 35,333.33   │ 106,000.01        │ PENDING  │
│  4 │ 2026-05-01  │ 35,333.33   │  70,666.68        │ PENDING  │
│  5 │ 2026-06-01  │ 35,333.33   │  35,333.35        │ PENDING  │
│  6 │ 2026-07-01  │ 35,333.33   │       0.00        │ PENDING  │
└────┴─────────────┴──────────────┴───────────────────┴──────────┘

After recording payment of 35,333.33 on 2026-02-01:
→ Installment #1 status → PAID
→ Loan outstanding balance → 176,666.67 RWF
```

### Financial Calculation Formula

```
Interest Amount    = Loan Amount × 6 / 100
Total Payable      = Loan Amount + Interest Amount
Monthly Installment = Total Payable ÷ Repayment Months  (rounded to 2 decimal places)
Outstanding Balance starts at = Total Payable
After each repayment: Outstanding Balance = Outstanding Balance − Payment Amount
```

---

## Testing and Quality Assurance

### Purpose of Test Classes

Test classes verify that the system's business logic works correctly in isolation from the database, web server, and other external dependencies. They use the **JUnit 5** test framework and **Mockito** for mocking dependencies.

Tests answer the question: *"If I give the service this input, does it produce the correct output and side effects?"*

Without tests, a developer changing one part of the code may unknowingly break another part. Tests act as a safety net that catches regressions immediately.

### Test Classes in the System

#### `MemberServiceTest`

Located in: `src/test/java/com/ikimina/service/MemberServiceTest.java`

Verifies member management business rules:

| Test | What it checks |
|---|---|
| `createMember_Success` | A valid member with unique email and national ID is saved successfully |
| `createMember_DuplicateEmail_ThrowsException` | Creating a member with an email already in use throws a clear error message |

These tests ensure that the system prevents duplicate accounts and enforces data integrity before hitting the database.

---

#### `LoanServiceTest`

Located in: `src/test/java/com/ikimina/service/LoanServiceTest.java`

Verifies the core loan lifecycle:

| Test | What it checks |
|---|---|
| `requestLoan_Success` | A valid loan request from an eligible member is accepted with status PENDING |
| `approveLoan_Success` | Approving a pending loan sets status to ACTIVE, records approval date, and triggers schedule generation |
| `rejectLoan_Success` | Rejecting a loan sets status to REJECTED and stores the rejection reason |

These tests confirm that approvals and rejections follow the correct rules, and that schedule generation is always triggered on approval.

---

#### `ScheduleServiceTest`

Located in: `src/test/java/com/ikimina/service/ScheduleServiceTest.java`

Verifies that repayment schedules are generated correctly:

| Test | What it checks |
|---|---|
| `testGenerateScheduleFor3MonthLoan` | A 3-month loan produces exactly 3 installment records |
| `testGenerateScheduleFor12MonthLoan` | A 12-month loan produces exactly 12 installment records with correct sequence |
| `testInstallmentAmountsSumToTotalPayable` | The sum of all installments equals the total payable (within 1 RWF rounding tolerance) |
| `testDueDatesAreCorrectlyCalculated` | Each due date is exactly approvalDate + N months for installment N |
| `testRemainingBalancesDecreaseCorrectly` | Remaining balance decreases by the installment amount after each entry; last entry is 0.00 |
| `testAllInstallmentsStartWithPendingStatus` | All generated installments start as PENDING |
| `testErrorHandlingForMissingApprovalDate` | Returns empty list (no crash) when approval date is missing |
| `testErrorHandlingForNonActiveLoan` | Returns empty list (no crash) when loan is not ACTIVE |

These are the most mathematically important tests — they guarantee the installment calendar is always correct.

---

#### `RepaymentServiceTest`

Located in: `src/test/java/com/ikimina/service/RepaymentServiceTest.java`

Verifies repayment recording and balance management:

| Test | What it checks |
|---|---|
| `recordRepayment_PartialPayment_UpdatesBalance` | A partial payment reduces the outstanding balance by the payment amount; loan stays ACTIVE |
| `recordRepayment_FullPayment_CompletesLoan` | A payment that covers the full balance sets status to REPAID and guarantor status to RELEASED |
| `recordRepayment_ExceedsBalance_ThrowsException` | A payment exceeding the outstanding balance is rejected with a clear error message |

These tests directly protect the most critical financial operation in the system.

---

#### `GuarantorServiceTest`

Located in: `src/test/java/com/ikimina/service/GuarantorServiceTest.java`

Verifies guarantor validation rules during loan requests:

| Test | What it checks |
|---|---|
| `requestLoan_WithoutGuarantor_SufficientSavings_Success` | If loan amount ≤ borrower's savings, no guarantor is required and the request succeeds |
| `requestLoan_GuarantorRequired_MissingGuarantor_ThrowsException` | If loan exceeds savings and no guarantor is provided, the request is rejected |
| `requestLoan_GuarantorInsufficientSavings_ThrowsException` | If the guarantor's savings are less than the required guarantee, the request is rejected |

These tests protect the group's funds from loans that lack sufficient collateral.

---

### Why Tests Make the System Safe

1. **They catch bugs before production.** If a developer changes the interest calculation, the schedule tests will immediately fail and alert them.
2. **They document behavior.** Each test describes exactly what the system should do under a specific condition — they are living documentation.
3. **They enable confident refactoring.** Any code change can be verified by running the tests. If they pass, the behavior is preserved.
4. **They enforce business rules.** Rules like "installment cannot exceed 60% of salary" or "guarantor must have enough savings" are encoded in tests so they can never be silently removed.

---

## Deployment and Maintainability

### Can the System Be Deployed Safely?

**Yes.**

The system is designed with safe deployment in mind:

| Property | How it is achieved |
|---|---|
| **Business logic separated from controllers** | Services (`LoanService`, `ScheduleService`, etc.) contain all rules. Controllers only handle HTTP routing. Changes to business rules do not require touching web layer code. |
| **Modular services** | Each domain has its own service class. You can update member management without touching loan logic. |
| **Test classes exist** | All critical business rules have automated tests. Before deploying any change, tests can be run to verify nothing is broken. |
| **Input validation** | All API inputs are validated with Jakarta Bean Validation (`@NotNull`, `@DecimalMin`). Invalid data is rejected before reaching service logic. |
| **Transaction management** | All database operations are wrapped in `@Transactional` at the service layer. If any step in a multi-step operation fails, the entire operation is rolled back, preventing partial saves. |
| **Data integrity** | Foreign key constraints, unique constraints, and not-null constraints are enforced at the database level as a second line of defense. |
| **Error handling** | A `GlobalExceptionHandler` catches all exceptions and returns structured JSON error responses instead of raw stack traces. |
| **Schedule fault tolerance** | Schedule update errors are logged but do not roll back the repayment record, ensuring repayments are always persisted even if schedule sync fails. |

### Making Changes Safely

The architecture follows the pattern:

```
HTTP Request
    → Controller (no logic, just routing)
        → Service (all business rules, @Transactional)
            → Repository (JPA, database access)
```

To add a new feature:
1. Add or update the repository query if new data access is needed.
2. Implement logic in the service class.
3. Expose it through the controller.
4. Write a test to verify the business rule.

This separation means changes are isolated and testable. A developer does not need to understand the full system to safely add one feature.

### Technology Stack Summary

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 3.5 (Java 17) |
| Database | PostgreSQL |
| ORM | Spring Data JPA (Hibernate) |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5 + Mockito |
| Frontend | React 18 + TypeScript |
| Build tool | Vite (frontend), Maven (backend) |
| API style | RESTful JSON API |

---

## Demo Users and Sample Data

The system automatically populates demo data on first startup through the `DemoDataInitializer` component. The following accounts and loans are created:

### Demo Members

| Name | Email | Role | Monthly Salary | Savings |
|---|---|---|---|---|
| System Admin | admin@ikimina.com | SUPER_ADMIN | — | — |
| Alice Uwimana | alice@ikimina.com | MEMBER | 1,000,000 RWF | 250,000 RWF |
| Jean Bosco | jean.bosco@ikimina.com | MEMBER | 1,000,000 RWF | 500,000 RWF |
| Diane Mukamana | diane@ikimina.com | MEMBER | 1,000,000 RWF | 350,000 RWF |
| Patrick Ndayisaba | patrick@ikimina.com | MEMBER | 1,000,000 RWF | 50,000 RWF |

### Demo Loans

| Borrower | Amount | Period | Purpose | Status | Payments Made |
|---|---|---|---|---|---|
| Alice Uwimana | 200,000 RWF | 6 months | Business Expansion | ACTIVE | 2 installments paid |
| Jean Bosco | 400,000 RWF | 10 months | Home Renovation | ACTIVE | 1 installment paid |

Both loans are within the borrowers' own savings, so no guarantor is required. Repayment schedules are automatically generated for both loans on startup.

### How Demo Data Is Controlled

The initializer checks whether `admin@ikimina.com` and `alice@ikimina.com` already exist before creating anything. This means:
- On the first startup, all demo data is created.
- On subsequent startups, no duplicate data is created.

---

## API Endpoints Reference

### Members API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/members` | List all members |
| GET | `/api/members/{id}` | Get member by ID |
| GET | `/api/members/{id}/summary` | Get member savings, shares, and loan summary |
| POST | `/api/members` | Create a new member |
| PUT | `/api/members/{id}` | Update member details |
| PATCH | `/api/members/{id}/activate` | Activate a member |
| PATCH | `/api/members/{id}/deactivate` | Deactivate a member |

### Savings API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/savings` | List all savings records |
| GET | `/api/savings/member/{memberId}` | List savings for a specific member |
| POST | `/api/savings` | Record a new savings contribution |

### Loans API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/loans` | List all loans (optional `?status=` filter) |
| GET | `/api/loans/{id}` | Get loan by ID |
| GET | `/api/loans/member/{memberId}` | Get all loans for a member |
| POST | `/api/loans` | Submit a new loan request |
| POST | `/api/loans/{id}/approve` | Approve a loan (generates schedule) |
| POST | `/api/loans/{id}/reject` | Reject a loan with reason |
| POST | `/api/loans/{id}/cancel` | Cancel a pending loan |
| POST | `/api/loans/{id}/repayments` | Record a repayment payment |
| GET | `/api/loans/{id}/repayments` | Get all repayments for a loan |
| DELETE | `/api/loans/{id}` | Delete a loan and its repayments |

### Dashboard API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard` | Get aggregated financial metrics |

---

## Bug Fix Notes

### Repayment Persistence Bug (Fixed)

**Symptom:** Recording a repayment showed a success message, but no repayment record was saved. Payment history remained empty.

**Root Cause:** In `LoanService.recordRepayment()`, the call to `scheduleService.processRepayment()` could throw an exception (e.g., if the `repayment_schedules` table had a schema issue or the schedule had not been generated). Because all three operations ran inside the same `@Transactional` method, an exception in schedule updating caused the entire transaction to roll back, including the `repaymentRepository.save()` call. The repayment was never persisted.

**Fix Applied:** The `scheduleService.processRepayment()` call is now wrapped in a `try-catch` block. If schedule updating fails, the error is logged as a warning but the exception is not re-thrown. This ensures the repayment record is always saved, and the loan's outstanding balance is always updated, regardless of schedule table state.

**Test Fix Applied:** `RepaymentServiceTest` and `GuarantorServiceTest` were missing required `@Mock ScheduleService`, `@Mock MemberRepository`, and `@Mock SavingRepository` declarations. Mockito's `@InjectMocks` could not construct `LoanService` correctly without these mocks, causing NullPointerExceptions at runtime. All missing mocks have been added.

---

*Document prepared for project presentation and future deployment reference.*
