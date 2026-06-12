export function formatMoney(value?: number) {
  const amount = new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 0,
  }).format(value ?? 0);
  return `RWF ${amount}`;
}

export function formatDate(value?: string) {
  if (!value) return 'Not set';
  return new Intl.DateTimeFormat('en-RW', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(value));
}

export function initials(name?: string) {
  if (!name) return 'DI';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

export function statusClass(status?: string) {
  const normalized = status?.toLowerCase() ?? '';
  if (['active', 'approved', 'accepted', 'paid', 'repaid'].includes(normalized)) return 'success';
  if (['pending', 'partial'].includes(normalized)) return 'warning';
  if (['rejected', 'defaulted', 'failed', 'cancelled'].includes(normalized)) return 'danger';
  return 'neutral';
}
