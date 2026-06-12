# Digital Ikimina API Testing Guide

Base URL:

```text
http://localhost:8080
```

Use `Content-Type: application/json` for POST and PUT requests.

## Members

Create a member:

```http
POST /api/members
```

```json
{
  "fullName": "Alice Uwase",
  "email": "alice@example.com",
  "phone": "0788000001",
  "nationalId": "1199980000000001",
  "monthlySalary": 500000,
  "contributionPercentage": 10,
  "joinDate": "2026-01-01",
  "role": "MEMBER"
}
```

Create a guarantor:

```http
POST /api/members
```

```json
{
  "fullName": "Jean Habimana",
  "email": "jean@example.com",
  "phone": "0788000002",
  "nationalId": "1199980000000002",
  "monthlySalary": 700000,
  "contributionPercentage": 12,
  "joinDate": "2026-01-01",
  "role": "MEMBER"
}
```

Other member endpoints:

```http
GET    /api/members
GET    /api/members/{id}
GET    /api/members/{id}/summary
PUT    /api/members/{id}
PATCH  /api/members/{id}/activate
PATCH  /api/members/{id}/deactivate
DELETE /api/members/{id}
```

Valid roles:

```text
MEMBER, ACCOUNTANT_ADMIN, LOAN_COMMITTEE, SECRETARY, SUPER_ADMIN
```

## Savings

Record monthly savings:

```http
POST /api/savings
```

```json
{
  "memberId": 1,
  "amount": 100000,
  "savingMonth": "2026-05"
}
```

Record guarantor savings:

```json
{
  "memberId": 2,
  "amount": 300000,
  "savingMonth": "2026-05"
}
```

Other saving endpoints:

```http
GET    /api/savings
GET    /api/savings/{id}
GET    /api/savings/member/{memberId}
GET    /api/savings/member/{memberId}/total
DELETE /api/savings/{id}
```

Rules:

```text
Minimum saving amount is 5000 RWF.
Only one saving record is allowed per member per savingMonth.
```

## Loans

Request a loan within member savings:

```http
POST /api/loans
```

```json
{
  "memberId": 1,
  "amount": 80000,
  "repaymentMonths": 6,
  "purpose": "School fees"
}
```

Request a loan above member savings using a guarantor:

```json
{
  "memberId": 1,
  "amount": 200000,
  "repaymentMonths": 6,
  "purpose": "Emergency support",
  "guarantorId": 2,
  "guaranteeAmount": 100000
}
```

Approve a pending loan:

```http
POST /api/loans/{id}/approve
```

Reject a pending loan:

```http
POST /api/loans/{id}/reject
```

```json
{
  "reason": "Insufficient repayment ability"
}
```

Record repayment:

```http
POST /api/loans/{id}/repayments
```

```json
{
  "amount": 20000,
  "paymentDate": "2026-05-28",
  "note": "Salary deduction"
}
```

Other loan endpoints:

```http
GET    /api/loans
GET    /api/loans?status=PENDING
GET    /api/loans?status=ACTIVE
GET    /api/loans/{id}
GET    /api/loans/member/{memberId}
GET    /api/loans/{id}/repayments
POST   /api/loans/{id}/cancel
DELETE /api/loans/{id}
```

Loan rules:

```text
Member must be active.
Member must have joined at least 3 months ago.
Interest is 6%.
Monthly installment cannot exceed 60% of salary when salary is provided.
Borrowing above total savings requires a guarantor.
Guarantor must be active and have enough savings for the guarantee amount.
```

Valid loan statuses:

```text
PENDING, APPROVED, REJECTED, ACTIVE, REPAID, DEFAULTED, CANCELLED
```

## Dashboard

```http
GET /api/dashboard
```

Returns:

```json
{
  "totalMembers": 2,
  "activeMembers": 2,
  "totalLoans": 1,
  "pendingLoans": 0,
  "totalSavings": 400000,
  "outstandingLoans": 84800,
  "totalRepayments": 20000,
  "expectedInterest": 4800
}
```
