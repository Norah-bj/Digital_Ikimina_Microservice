# Requirements Document

## Introduction

The Loan Repayment Schedule System extends the existing Digital Ikimina platform by automatically generating and tracking detailed installment-by-installment repayment schedules for approved loans. Currently, the system calculates monthly installments and tracks actual repayments, but lacks a structured schedule that shows all planned installments with their due dates, amounts, and statuses. This feature provides transparency and helps members understand their repayment obligations while enabling administrators to track payment progress more effectively.

The system will generate schedules automatically when loans are approved, display them in loan detail views, update installment statuses based on actual payments, and mark overdue installments appropriately.

## Glossary

- **Loan_Service**: The backend service component responsible for loan management operations including approval, repayment tracking, and schedule generation
- **Schedule_Generator**: The component responsible for creating installment records when a loan is approved
- **Installment**: A single scheduled repayment entry within a loan repayment schedule, containing due date, amount, status, and remaining balance information
- **Repayment_Schedule**: The complete collection of installment records for a specific loan
- **Installment_Status**: The current state of an installment (PENDING, PAID, OVERDUE, PARTIALLY_PAID)
- **Schedule_Repository**: The data access component for persisting and retrieving installment records
- **Loan_Details_Page**: The frontend view displaying comprehensive loan information including the repayment schedule
- **Demo_Data_Seeder**: The component responsible for creating comprehensive test data including users, members, loans, and schedules
- **Schedule_Updater**: The component responsible for updating installment statuses based on actual repayments

## Requirements

### Requirement 1: Generate Repayment Schedule on Loan Approval

**User Story:** As a loan administrator, I want repayment schedules to be automatically generated when I approve a loan, so that members have a clear breakdown of their payment obligations.

#### Acceptance Criteria

1. WHEN a loan status changes to ACTIVE, THE Schedule_Generator SHALL create installment records equal to the loan repayment months
2. FOR EACH installment record, THE Schedule_Generator SHALL calculate the due date as approval date plus N months where N is the installment number
3. FOR EACH installment record, THE Schedule_Generator SHALL set the installment amount equal to the loan monthly installment
4. FOR EACH installment record, THE Schedule_Generator SHALL calculate the remaining balance as total payable minus sum of all prior installments
5. FOR EACH installment record, THE Schedule_Generator SHALL set the initial status to PENDING
6. THE Schedule_Generator SHALL persist all installment records to the Schedule_Repository
7. WHEN schedule generation fails, THE Loan_Service SHALL log the error and continue loan approval without interruption

### Requirement 2: Store Installment Details

**User Story:** As a system administrator, I want installment details to be stored in the database, so that schedules persist and can be queried efficiently.

#### Acceptance Criteria

1. THE Schedule_Repository SHALL store installment number as a positive integer
2. THE Schedule_Repository SHALL store due date as a date value
3. THE Schedule_Repository SHALL store installment amount as a decimal with precision 19 and scale 2
4. THE Schedule_Repository SHALL store installment status as an enumeration
5. THE Schedule_Repository SHALL store remaining balance as a decimal with precision 19 and scale 2
6. THE Schedule_Repository SHALL store a foreign key reference to the parent loan
7. THE Schedule_Repository SHALL create a unique constraint on loan ID and installment number combinations
8. THE Schedule_Repository SHALL create an index on loan ID for efficient schedule retrieval

### Requirement 3: Retrieve Repayment Schedule by Loan

**User Story:** As a member, I want to view my complete repayment schedule, so that I understand when each payment is due and how much I owe.

#### Acceptance Criteria

1. WHEN a loan ID is provided, THE Loan_Service SHALL retrieve all installment records for that loan ordered by installment number ascending
2. WHEN the loan has no installment records, THE Loan_Service SHALL return an empty collection
3. THE Loan_Service SHALL include installment number, due date, amount, status, and remaining balance in each returned record
4. THE Loan_Service SHALL validate that the loan ID exists before querying installments
5. WHEN the loan ID does not exist, THE Loan_Service SHALL return an error message indicating loan not found

### Requirement 4: Display Repayment Schedule in Loan Details

**User Story:** As a member, I want to see my repayment schedule in the loan details page, so that I can track my payment progress.

#### Acceptance Criteria

1. WHEN a loan is selected, THE Loan_Details_Page SHALL display a repayment schedule section
2. THE Loan_Details_Page SHALL display each installment with installment number, due date, installment amount, status, and remaining balance
3. THE Loan_Details_Page SHALL format amounts as currency with RWF symbol and two decimal places
4. THE Loan_Details_Page SHALL format dates as DD-MM-YYYY
5. THE Loan_Details_Page SHALL display status as a colored badge where PAID is green, PENDING is yellow, OVERDUE is red, and PARTIALLY_PAID is orange
6. WHEN no schedule exists for the loan, THE Loan_Details_Page SHALL display a message indicating no schedule is available
7. THE Loan_Details_Page SHALL display installments in ascending order by installment number

### Requirement 5: Update Installment Status Based on Repayments

**User Story:** As an accountant, I want installment statuses to update automatically when I record repayments, so that the schedule reflects actual payment progress.

#### Acceptance Criteria

1. WHEN a repayment is recorded, THE Schedule_Updater SHALL identify unpaid installments for that loan ordered by due date ascending
2. THE Schedule_Updater SHALL allocate the repayment amount to unpaid installments starting with the earliest due date
3. WHEN a repayment amount fully covers an installment, THE Schedule_Updater SHALL set that installment status to PAID
4. WHEN a repayment amount partially covers an installment, THE Schedule_Updater SHALL set that installment status to PARTIALLY_PAID
5. WHEN a repayment amount exceeds a single installment, THE Schedule_Updater SHALL apply the excess to the next installment
6. THE Schedule_Updater SHALL update the remaining balance for each affected installment
7. THE Schedule_Updater SHALL persist all installment status changes to the Schedule_Repository

### Requirement 6: Mark Overdue Installments

**User Story:** As a loan administrator, I want installments to be automatically marked as overdue when their due date passes without payment, so that I can identify delinquent accounts.

#### Acceptance Criteria

1. THE Schedule_Updater SHALL identify all installments with status PENDING where due date is before the current date
2. FOR EACH overdue installment, THE Schedule_Updater SHALL set the status to OVERDUE
3. THE Schedule_Updater SHALL execute the overdue check on a scheduled basis daily at midnight
4. THE Schedule_Updater SHALL persist all status changes to the Schedule_Repository
5. WHEN an OVERDUE installment is subsequently paid, THE Schedule_Updater SHALL set the status to PAID

### Requirement 7: Create Comprehensive Demo Data

**User Story:** As a developer, I want comprehensive demo data including users, members, loans, and schedules, so that I can test and demonstrate the complete system functionality.

#### Acceptance Criteria

1. THE Demo_Data_Seeder SHALL create an admin user with username admin and password admin123
2. THE Demo_Data_Seeder SHALL create exactly 4 demo members with varying join dates spanning 1 to 6 months before the current date
3. THE Demo_Data_Seeder SHALL assign total savings between 50000 and 500000 RWF to each demo member
4. THE Demo_Data_Seeder SHALL create at least 2 approved loans for eligible members with varying repayment terms between 3 and 12 months
5. WHEN a demo loan is created with ACTIVE status, THE Demo_Data_Seeder SHALL trigger the Schedule_Generator to create the repayment schedule
6. THE Demo_Data_Seeder SHALL create at least 2 recorded repayments for at least one demo loan to demonstrate installment status updates
7. THE Demo_Data_Seeder SHALL execute only when no admin user exists to prevent duplicate data
8. THE Demo_Data_Seeder SHALL log all created entities for verification purposes

### Requirement 8: Provide Schedule Summary in Loan Entity

**User Story:** As a frontend developer, I want loan entities to include schedule summary information, so that I can display payment progress without additional queries.

#### Acceptance Criteria

1. WHEN a loan is retrieved, THE Loan_Service SHALL calculate the count of installments with status PAID
2. WHEN a loan is retrieved, THE Loan_Service SHALL calculate the count of installments with status OVERDUE
3. WHEN a loan is retrieved, THE Loan_Service SHALL calculate the count of installments with status PENDING
4. WHEN a loan is retrieved, THE Loan_Service SHALL calculate the count of installments with status PARTIALLY_PAID
5. THE Loan_Service SHALL include these counts in the loan response as paid_installments, overdue_installments, pending_installments, and partially_paid_installments
6. WHEN no installments exist for a loan, THE Loan_Service SHALL return zero for all installment counts

### Requirement 9: Validate Schedule Consistency

**User Story:** As a system administrator, I want the system to validate schedule consistency, so that repayment calculations remain accurate.

#### Acceptance Criteria

1. WHEN retrieving a repayment schedule, THE Loan_Service SHALL verify that the count of installments equals the loan repayment months
2. WHEN retrieving a repayment schedule, THE Loan_Service SHALL verify that the sum of all installment amounts equals the loan total payable within 1 RWF tolerance for rounding differences
3. WHEN retrieving a repayment schedule, THE Loan_Service SHALL verify that each installment has a unique installment number between 1 and the repayment months
4. WHEN a consistency validation fails, THE Loan_Service SHALL log a warning with the loan ID and validation error details
5. THE Loan_Service SHALL return the schedule data even when consistency validation fails

### Requirement 10: Support Backend API for Schedule Operations

**User Story:** As a frontend developer, I want REST API endpoints for schedule operations, so that I can integrate schedule functionality into the user interface.

#### Acceptance Criteria

1. THE Loan_Service SHALL expose an endpoint GET /api/loans/{loanId}/schedule that returns the complete repayment schedule
2. THE Loan_Service SHALL expose an endpoint POST /api/loans/{loanId}/schedule/regenerate that deletes and recreates the schedule for ACTIVE loans
3. THE Loan_Service SHALL expose an endpoint GET /api/loans/{loanId}/schedule/summary that returns installment status counts
4. WHEN an unauthorized user requests a schedule endpoint, THE Loan_Service SHALL return HTTP status 403 Forbidden
5. WHEN a loan ID does not exist, THE Loan_Service SHALL return HTTP status 404 Not Found
6. THE Loan_Service SHALL return schedule data as JSON with proper content type headers
7. THE Loan_Service SHALL validate request parameters and return HTTP status 400 Bad Request for invalid inputs
