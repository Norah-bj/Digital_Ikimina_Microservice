import { FormEvent, useState } from 'react';
import type { MemberPayload, MemberRole } from '../types';

const roles: MemberRole[] = ['MEMBER', 'ACCOUNTANT_ADMIN', 'LOAN_COMMITTEE', 'SECRETARY', 'SUPER_ADMIN'];

interface MemberFormProps {
  onSubmit: (payload: MemberPayload) => void;
  busy?: boolean;
  initialValue?: MemberPayload;
  submitLabel?: string;
}

export default function MemberForm({ onSubmit, busy, initialValue, submitLabel = 'Create member' }: MemberFormProps) {
  const [payload, setPayload] = useState<MemberPayload>({
    fullName: initialValue?.fullName ?? '',
    email: initialValue?.email ?? '',
    phone: initialValue?.phone ?? '',
    nationalId: initialValue?.nationalId ?? '',
    monthlySalary: initialValue?.monthlySalary ?? 0,
    contributionPercentage: initialValue?.contributionPercentage ?? 5,
    joinDate: initialValue?.joinDate ?? new Date().toISOString().slice(0, 10),
    role: initialValue?.role ?? 'MEMBER',
  });

  function update<K extends keyof MemberPayload>(key: K, value: MemberPayload[K]) {
    setPayload((current) => ({ ...current, [key]: value }));
  }

  function submit(event: FormEvent) {
    event.preventDefault();
    onSubmit(payload);
  }

  return (
    <form className="form-grid" onSubmit={submit}>
      <label>
        <span>Full name</span>
        <input required value={payload.fullName} onChange={(event) => update('fullName', event.target.value)} />
      </label>
      <label>
        <span>Email</span>
        <input required type="email" value={payload.email} onChange={(event) => update('email', event.target.value)} />
      </label>
      <label>
        <span>Phone</span>
        <input value={payload.phone} onChange={(event) => update('phone', event.target.value)} />
      </label>
      <label>
        <span>National ID</span>
        <input value={payload.nationalId} onChange={(event) => update('nationalId', event.target.value)} />
      </label>
      <label>
        <span>Monthly salary</span>
        <input type="number" min="0" value={payload.monthlySalary} onChange={(event) => update('monthlySalary', Number(event.target.value))} />
      </label>
      <label>
        <span>Contribution %</span>
        <input type="number" min="0" value={payload.contributionPercentage} onChange={(event) => update('contributionPercentage', Number(event.target.value))} />
      </label>
      <label>
        <span>Join date</span>
        <input type="date" value={payload.joinDate} onChange={(event) => update('joinDate', event.target.value)} />
      </label>
      <label>
        <span>Role</span>
        <select value={payload.role} onChange={(event) => update('role', event.target.value as MemberRole)}>
          {roles.map((role) => (
            <option key={role} value={role}>
              {role.replaceAll('_', ' ')}
            </option>
          ))}
        </select>
      </label>
      <button className="button primary form-submit" disabled={busy}>
        {busy ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}
