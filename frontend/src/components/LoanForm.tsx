import { FormEvent, useState } from 'react';
import type { LoanPayload, Member } from '../types';

interface LoanFormProps {
  members: Member[];
  onSubmit: (payload: LoanPayload) => void;
  busy?: boolean;
}

export default function LoanForm({ members, onSubmit, busy }: LoanFormProps) {
  const [payload, setPayload] = useState<LoanPayload>({
    memberId: members[0]?.id ?? 0,
    amount: 100000,
    repaymentMonths: 6,
    purpose: '',
    guarantorId: undefined,
    guaranteeAmount: undefined,
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit({
      ...payload,
      guarantorId: payload.guarantorId || undefined,
      guaranteeAmount: payload.guaranteeAmount || undefined,
    });
  }

  return (
    <form className="form-grid" onSubmit={submit}>
      <label>
        <span>Borrower</span>
        <select value={payload.memberId} onChange={(event) => setPayload({ ...payload, memberId: Number(event.target.value) })}>
          <option value={0}>Select borrower</option>
          {members.map((member) => (
            <option key={member.id} value={member.id}>
              {member.fullName}
            </option>
          ))}
        </select>
      </label>
      <label>
        <span>Amount</span>
        <input type="number" min="1" value={payload.amount} onChange={(event) => setPayload({ ...payload, amount: Number(event.target.value) })} />
      </label>
      <label>
        <span>Repayment months</span>
        <input type="number" min="1" max="36" value={payload.repaymentMonths} onChange={(event) => setPayload({ ...payload, repaymentMonths: Number(event.target.value) })} />
      </label>
      <label>
        <span>Guarantor</span>
        <select value={payload.guarantorId ?? 0} onChange={(event) => setPayload({ ...payload, guarantorId: Number(event.target.value) || undefined })}>
          <option value={0}>Not required</option>
          {members
            .filter((member) => member.id !== payload.memberId)
            .map((member) => (
              <option key={member.id} value={member.id}>
                {member.fullName}
              </option>
            ))}
        </select>
      </label>
      <label>
        <span>Guarantee amount</span>
        <input type="number" min="0" value={payload.guaranteeAmount ?? ''} onChange={(event) => setPayload({ ...payload, guaranteeAmount: Number(event.target.value) || undefined })} />
      </label>
      <label className="span-2">
        <span>Purpose</span>
        <input value={payload.purpose} onChange={(event) => setPayload({ ...payload, purpose: event.target.value })} />
      </label>
      <button className="button primary form-submit" disabled={busy || !payload.memberId}>
        {busy ? 'Submitting...' : 'Request loan'}
      </button>
    </form>
  );
}
