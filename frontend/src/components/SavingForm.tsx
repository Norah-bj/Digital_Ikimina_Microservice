import { FormEvent, useState } from 'react';
import type { Member, SavingPayload } from '../types';

interface SavingFormProps {
  members: Member[];
  onSubmit: (payload: SavingPayload) => void;
  busy?: boolean;
}

export default function SavingForm({ members, onSubmit, busy }: SavingFormProps) {
  const [payload, setPayload] = useState<SavingPayload>({
    memberId: members[0]?.id ?? 0,
    amount: 5000,
    savingMonth: new Date().toISOString().slice(0, 7),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit(payload);
  }

  return (
    <form className="form-grid compact" onSubmit={submit}>
      <label>
        <span>Member</span>
        <select value={payload.memberId} onChange={(event) => setPayload({ ...payload, memberId: Number(event.target.value) })}>
          <option value={0}>Select member</option>
          {members.map((member) => (
            <option key={member.id} value={member.id}>
              {member.fullName}
            </option>
          ))}
        </select>
      </label>
      <label>
        <span>Amount</span>
        <input type="number" min="5000" value={payload.amount} onChange={(event) => setPayload({ ...payload, amount: Number(event.target.value) })} />
      </label>
      <label>
        <span>Month</span>
        <input type="month" value={payload.savingMonth} onChange={(event) => setPayload({ ...payload, savingMonth: event.target.value })} />
      </label>
      <button className="button primary form-submit" disabled={busy || !payload.memberId}>
        {busy ? 'Recording...' : 'Record saving'}
      </button>
    </form>
  );
}
