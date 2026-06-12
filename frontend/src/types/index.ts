export type MemberRole = 'MEMBER' | 'ACCOUNTANT_ADMIN' | 'LOAN_COMMITTEE' | 'SECRETARY' | 'SUPER_ADMIN';

export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE' | 'REPAID' | 'DEFAULTED' | 'CANCELLED';

export type GuaranteeStatus = 'NOT_REQUIRED' | 'PENDING' | 'ACCEPTED' | 'RELEASED';

export interface Member {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  nationalId?: string;
  monthlySalary: number;
  contributionPercentage: number;
  joinDate: string;
  isActive: boolean;
  role: MemberRole;
  savings?: Saving[];
  loans?: Loan[];
}

export interface Saving {
  id: number;
  amount: number;
  savingMonth: string;
  createdAt: string;
  member: Pick<Member, 'id' | 'fullName' | 'email' | 'role' | 'isActive'>;
}

export interface Loan {
  id: number;
  amount: number;
  status: LoanStatus;
  requestDate: string;
  approvalDate?: string;
  dueDate?: string;
  interestRate: number;
  interestAmount: number;
  totalPayable: number;
  outstandingBalance: number;
  repaymentMonths: number;
  monthlyInstallment: number;
  purpose?: string;
  rejectionReason?: string;
  guaranteeStatus: GuaranteeStatus;
  guaranteeAmount?: number;
  member: Pick<Member, 'id' | 'fullName' | 'email' | 'role' | 'isActive'>;
  guarantor?: Pick<Member, 'id' | 'fullName' | 'email' | 'role' | 'isActive'>;
}

export interface Repayment {
  id: number;
  amount: number;
  paymentDate: string;
  remainingBalance: number;
  status: 'PAID' | 'PARTIAL' | 'FAILED';
  note?: string;
  loan: Pick<Loan, 'id' | 'amount' | 'status' | 'outstandingBalance'>;
}

export interface Dashboard {
  totalMembers: number;
  activeMembers: number;
  totalLoans: number;
  pendingLoans: number;
  totalSavings: number;
  outstandingLoans: number;
  totalRepayments: number;
  expectedInterest: number;
}

export interface MemberSummary {
  memberId: number;
  fullName: string;
  totalSavings: number;
  shareValue: number;
  activeLoanBalance: number;
  monthlyContributionEstimate: number;
  savingsCount: number;
  loansCount: number;
}

export interface MemberPayload {
  fullName: string;
  email: string;
  phone?: string;
  nationalId?: string;
  monthlySalary: number;
  contributionPercentage: number;
  joinDate?: string;
  role: MemberRole;
}

export interface SavingPayload {
  memberId: number;
  amount: number;
  savingMonth: string;
}

export interface LoanPayload {
  memberId: number;
  amount: number;
  repaymentMonths: number;
  purpose?: string;
  guarantorId?: number;
  guaranteeAmount?: number;
}

export interface RepaymentPayload {
  amount: number;
  paymentDate?: string;
  note?: string;
}
