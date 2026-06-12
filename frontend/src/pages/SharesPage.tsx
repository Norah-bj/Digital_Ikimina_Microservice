import { useQuery } from '@tanstack/react-query';
import { BadgePercent } from 'lucide-react';
import EmptyState from '../components/EmptyState';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import { api } from '../services/api';
import { formatMoney } from '../utils/format';

export default function SharesPage() {
  const members = useQuery({ queryKey: ['members'], queryFn: api.members });
  const savings = useQuery({ queryKey: ['savings'], queryFn: api.savings });
  const totalSavings = (savings.data ?? []).reduce((sum, saving) => sum + saving.amount, 0);

  if (members.isLoading || savings.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Equity" title="Shares" description="Estimate member ownership based on accumulated contributions." />
      <ErrorBanner message={members.error?.message || savings.error?.message} />
      <Panel title="Share ownership" description="Savings-backed member equity view">
        {members.data?.length ? (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Member</th><th>Contribution value</th><th>Ownership</th><th>Transfer status</th></tr></thead>
              <tbody>
                {members.data.map((member) => {
                  const value = (savings.data ?? []).filter((saving) => saving.member.id === member.id).reduce((sum, saving) => sum + saving.amount, 0);
                  const ownership = totalSavings ? (value / totalSavings) * 100 : 0;
                  return (
                    <tr key={member.id}>
                      <td>{member.fullName}</td>
                      <td>{formatMoney(value)}</td>
                      <td>
                        <div className="progress-cell">
                          <span style={{ width: `${Math.min(ownership, 100)}%` }} />
                        </div>
                        {ownership.toFixed(1)}%
                      </td>
                      <td>Board approval required</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState icon={BadgePercent} title="No share records" message="Share ownership appears after members begin saving." />
        )}
      </Panel>
    </div>
  );
}
