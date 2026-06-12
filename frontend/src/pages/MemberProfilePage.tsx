import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, BadgePercent, HandCoins, PiggyBank, ShieldCheck } from 'lucide-react';
import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import EmptyState from '../components/EmptyState';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import MetricCard from '../components/MetricCard';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import StatusBadge from '../components/StatusBadge';
import { api } from '../services/api';
import { formatDate, formatMoney } from '../utils/format';

export default function MemberProfilePage() {
  const id = Number(useParams().id);
  const member = useQuery({ queryKey: ['member', id], queryFn: () => api.member(id), enabled: Boolean(id) });
  const summary = useQuery({ queryKey: ['member-summary', id], queryFn: () => api.memberSummary(id), enabled: Boolean(id) });
  const loans = useQuery({ queryKey: ['member-loans', id], queryFn: () => api.memberLoans(id), enabled: Boolean(id) });
  const savings = useQuery({ queryKey: ['member-savings', id], queryFn: () => api.memberSavings(id), enabled: Boolean(id) });

  if (member.isLoading || summary.isLoading) return <LoadingState />;

  const savingsTrend = (savings.data ?? []).map((saving) => ({ month: saving.savingMonth, amount: saving.amount }));

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Member profile"
        title={member.data?.fullName ?? 'Member'}
        description="Personal details, savings analytics, loan history, guarantees, and share position."
        actions={
          <Link to="/members" className="button secondary">
            <ArrowLeft size={15} />
            Back
          </Link>
        }
      />
      <ErrorBanner message={member.error?.message || summary.error?.message || loans.error?.message || savings.error?.message} />

      <section className="metrics-grid">
        <MetricCard title="Total savings" value={summary.data?.totalSavings ?? 0} icon={PiggyBank} />
        <MetricCard title="Share value" value={summary.data?.shareValue ?? 0} icon={BadgePercent} tone="cyan" />
        <MetricCard title="Active loan balance" value={summary.data?.activeLoanBalance ?? 0} icon={HandCoins} tone="amber" />
        <MetricCard title="Monthly contribution" value={summary.data?.monthlyContributionEstimate ?? 0} icon={ShieldCheck} tone="emerald" />
      </section>

      <section className="dashboard-grid">
        <Panel title="Personal info" description="Core member profile">
          <div className="detail-list">
            <div><span>Email</span><strong>{member.data?.email}</strong></div>
            <div><span>Phone</span><strong>{member.data?.phone || 'Not set'}</strong></div>
            <div><span>Role</span><strong>{member.data?.role.replaceAll('_', ' ')}</strong></div>
            <div><span>Joined</span><strong>{formatDate(member.data?.joinDate)}</strong></div>
            <div><span>Status</span><StatusBadge value={member.data?.isActive ? 'ACTIVE' : 'INACTIVE'} /></div>
          </div>
        </Panel>

        <Panel title="Savings analytics" description="Contribution behavior over time">
          {savingsTrend.length ? (
            <div className="chart-box compact-chart">
              <ResponsiveContainer width="100%" height={210}>
                <AreaChart data={savingsTrend}>
                  <XAxis dataKey="month" tickLine={false} axisLine={false} fontSize={11} />
                  <YAxis tickLine={false} axisLine={false} fontSize={11} tickFormatter={(value) => `${Number(value) / 1000}k`} />
                  <Tooltip formatter={(value) => formatMoney(Number(value))} />
                  <Area dataKey="amount" stroke="#0F766E" fill="#CCFBF1" strokeWidth={2} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <EmptyState icon={PiggyBank} title="No savings history" message="Savings analytics will appear after monthly contributions." />
          )}
        </Panel>
      </section>

      <section className="dashboard-grid">
        <Panel title="Loan history" description="Requests, approvals, and repayment state">
          {(loans.data ?? []).length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Amount</th><th>Installment</th><th>Due</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {loans.data?.map((loan) => (
                    <tr key={loan.id}>
                      <td>{formatMoney(loan.amount)}</td>
                      <td>{formatMoney(loan.monthlyInstallment)}</td>
                      <td>{formatDate(loan.dueDate)}</td>
                      <td><StatusBadge value={loan.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState icon={HandCoins} title="No loan records" message="Loan history will appear when this member requests financing." />
          )}
        </Panel>

        <Panel title="Guarantees and shares" description="Trust network and ownership position">
          <div className="detail-list">
            <div><span>Guarantee exposure</span><strong>{formatMoney((loans.data ?? []).reduce((sum, loan) => sum + (loan.guaranteeAmount ?? 0), 0))}</strong></div>
            <div><span>Savings records</span><strong>{summary.data?.savingsCount ?? 0}</strong></div>
            <div><span>Loan records</span><strong>{summary.data?.loansCount ?? 0}</strong></div>
            <div><span>Ownership basis</span><strong>Contribution shares</strong></div>
          </div>
        </Panel>
      </section>
    </div>
  );
}
