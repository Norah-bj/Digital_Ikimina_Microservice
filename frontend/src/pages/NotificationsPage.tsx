import { Bell, CheckCircle2, Clock3, TriangleAlert } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';

const notifications = [
  { icon: CheckCircle2, title: 'Loan approved', message: 'Loan committee approved a member request.', tone: 'success' },
  { icon: TriangleAlert, title: 'Repayment overdue', message: 'One active loan passed its due date.', tone: 'warning' },
  { icon: Clock3, title: 'Salary deduction completed', message: 'Monthly savings import is ready for review.', tone: 'neutral' },
];

export default function NotificationsPage() {
  return (
    <div className="page-stack">
      <PageHeader eyebrow="Messaging" title="Notifications" description="Operational alerts for approvals, repayments, salary deductions, and member events." />
      <Panel title="Notification center" description="Compact alerts for finance administrators">
        <div className="notification-list">
          {notifications.map((notification) => (
            <article className="notification-item" key={notification.title}>
              <span className={`icon-chip ${notification.tone}`}>
                <notification.icon size={16} />
              </span>
              <div>
                <strong>{notification.title}</strong>
                <p>{notification.message}</p>
              </div>
              <Bell size={14} />
            </article>
          ))}
        </div>
      </Panel>
    </div>
  );
}
