-- Run this against the ikimina_db PostgreSQL database if the app reports
-- missing columns. It updates older tables to match the current Java entities
-- without deleting existing records.

ALTER TABLE members
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone VARCHAR(255),
    ADD COLUMN IF NOT EXISTS national_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS monthly_salary NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS contribution_percentage NUMERIC(5, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS join_date DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS role VARCHAR(255) DEFAULT 'MEMBER' NOT NULL;

ALTER TABLE loans
    ADD COLUMN IF NOT EXISTS amount NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PENDING' NOT NULL,
    ADD COLUMN IF NOT EXISTS request_date DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS approval_date DATE,
    ADD COLUMN IF NOT EXISTS due_date DATE,
    ADD COLUMN IF NOT EXISTS interest_rate NUMERIC(5, 2) DEFAULT 6 NOT NULL,
    ADD COLUMN IF NOT EXISTS interest_amount NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS total_payable NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS outstanding_balance NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS repayment_months INTEGER DEFAULT 1 NOT NULL,
    ADD COLUMN IF NOT EXISTS monthly_installment NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS purpose VARCHAR(255),
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(255),
    ADD COLUMN IF NOT EXISTS guarantee_status VARCHAR(255) DEFAULT 'NOT_REQUIRED' NOT NULL,
    ADD COLUMN IF NOT EXISTS guarantor_id BIGINT,
    ADD COLUMN IF NOT EXISTS guarantee_amount NUMERIC(19, 2),
    ADD COLUMN IF NOT EXISTS member_id BIGINT;

ALTER TABLE savings
    ADD COLUMN IF NOT EXISTS amount NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS saving_month VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS member_id BIGINT;

ALTER TABLE repayments
    ADD COLUMN IF NOT EXISTS loan_id BIGINT,
    ADD COLUMN IF NOT EXISTS amount NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS payment_date DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS remaining_balance NUMERIC(19, 2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PAID' NOT NULL,
    ADD COLUMN IF NOT EXISTS note VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_loans_guarantor'
          AND table_name = 'loans'
    ) THEN
        ALTER TABLE loans
            ADD CONSTRAINT fk_loans_guarantor
            FOREIGN KEY (guarantor_id)
            REFERENCES members(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_loans_member'
          AND table_name = 'loans'
    ) THEN
        ALTER TABLE loans
            ADD CONSTRAINT fk_loans_member
            FOREIGN KEY (member_id)
            REFERENCES members(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_savings_member'
          AND table_name = 'savings'
    ) THEN
        ALTER TABLE savings
            ADD CONSTRAINT fk_savings_member
            FOREIGN KEY (member_id)
            REFERENCES members(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_repayments_loan'
          AND table_name = 'repayments'
    ) THEN
        ALTER TABLE repayments
            ADD CONSTRAINT fk_repayments_loan
            FOREIGN KEY (loan_id)
            REFERENCES loans(id);
    END IF;
END $$;
