import type {
  Dashboard,
  Loan,
  LoanPayload,
  LoanStatus,
  Member,
  MemberPayload,
  MemberSummary,
  Repayment,
  RepaymentPayload,
  Saving,
  SavingPayload,
} from '../types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    const message = payload?.error ?? payload?.message ?? `Request failed with ${response.status}`;
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  dashboard: () => request<Dashboard>('/api/dashboard'),

  members: () => request<Member[]>('/api/members'),
  member: (id: number) => request<Member>(`/api/members/${id}`),
  memberSummary: (id: number) => request<MemberSummary>(`/api/members/${id}/summary`),
  createMember: (payload: MemberPayload) =>
    request<Member>('/api/members', { method: 'POST', body: JSON.stringify(payload) }),
  updateMember: (id: number, payload: MemberPayload) =>
    request<Member>(`/api/members/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  activateMember: (id: number) => request<Member>(`/api/members/${id}/activate`, { method: 'PATCH' }),
  deactivateMember: (id: number) => request<Member>(`/api/members/${id}/deactivate`, { method: 'PATCH' }),

  savings: () => request<Saving[]>('/api/savings'),
  memberSavings: (memberId: number) => request<Saving[]>(`/api/savings/member/${memberId}`),
  createSaving: (payload: SavingPayload) =>
    request<Saving>('/api/savings', { method: 'POST', body: JSON.stringify(payload) }),

  loans: (status?: LoanStatus) => request<Loan[]>(status ? `/api/loans?status=${status}` : '/api/loans'),
  memberLoans: (memberId: number) => request<Loan[]>(`/api/loans/member/${memberId}`),
  createLoan: (payload: LoanPayload) =>
    request<Loan>('/api/loans', { method: 'POST', body: JSON.stringify(payload) }),
  approveLoan: (id: number) => request<Loan>(`/api/loans/${id}/approve`, { method: 'POST' }),
  rejectLoan: (id: number, reason: string) =>
    request<Loan>(`/api/loans/${id}/reject`, { method: 'POST', body: JSON.stringify({ reason }) }),
  cancelLoan: (id: number) => request<Loan>(`/api/loans/${id}/cancel`, { method: 'POST' }),

  repayments: (loanId: number) => request<Repayment[]>(`/api/loans/${loanId}/repayments`),
  createRepayment: (loanId: number, payload: RepaymentPayload) =>
    request<Repayment>(`/api/loans/${loanId}/repayments`, { method: 'POST', body: JSON.stringify(payload) }),
};
