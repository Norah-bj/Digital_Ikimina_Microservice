import { FormEvent, useState } from 'react';
import type { Loan, RepaymentPayload } from '../types';

interface RepaymentFormProps {
  loans: Loan[];
  onSubmit: (loanId: number, payload: RepaymentPayload) => void;
  busy?: boolean;
}

export default function RepaymentForm({ loans, onSubmit, busy }: RepaymentFormProps) {
  const activeLoans = loans.filter((loan) => loan.status === 'ACTIVE' || loan.status === 'DEFAULTED');
  const [loanId, setLoanId] = useState(activeLoans[0]?.id ?? 0);
  const [payload, setPayload] = useState<RepaymentPayload>({
    amount: 10000,
    paymentDate: new Date().toISOString().slice(0, 10),
    note: 'Salary deduction',
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit(loanId, payload);
  }

  return (
    <form className="form-grid compact" onSubmit={submit}>
      <label>
        <span>Loan</span>
        <select value={loanId} onChange={(event) => setLoanId(Number(event.target.value))}>
          <option value={0}>Select active loan</option>
          {activeLoans.map((loan) => (
            <option key={loan.id} value={loan.id}>
              #{loan.id} - {loan.member.fullName}
            </option>
          ))}
        </select>
      </label>
      <label>
        <span>Amount</span>
        <input type="number" min="1" value={payload.amount} onChange={(event) => setPayload({ ...payload, amount: Number(event.target.value) })} />
      </label>
      <label>
        <span>Date</span>
        <input type="date" value={payload.paymentDate} onChange={(event) => setPayload({ ...payload, paymentDate: event.target.value })} />
      </label>
      <label>
        <span>Note</span>
        <input value={payload.note} onChange={(event) => setPayload({ ...payload, note: event.target.value })} />
      </label>
      <button className="button primary form-submit" disabled={busy || !loanId}>
        {busy ? 'Posting...' : 'Record repayment'}
      </button>
    </form>
  );
}
