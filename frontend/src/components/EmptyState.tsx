import type { LucideIcon } from 'lucide-react';

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  message: string;
}

export default function EmptyState({ icon: Icon, title, message }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <span>
        <Icon size={22} />
      </span>
      <h3>{title}</h3>
      <p>{message}</p>
    </div>
  );
}
