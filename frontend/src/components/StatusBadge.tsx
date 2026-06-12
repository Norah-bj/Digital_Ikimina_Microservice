import { statusClass } from '../utils/format';

interface StatusBadgeProps {
  value?: string;
}

export default function StatusBadge({ value = 'UNKNOWN' }: StatusBadgeProps) {
  return <span className={`status-badge ${statusClass(value)}`}>{value.replaceAll('_', ' ')}</span>;
}
