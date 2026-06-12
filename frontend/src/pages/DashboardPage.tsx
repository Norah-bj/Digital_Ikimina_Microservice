import { useQuery } from '@tanstack/react-query';
import { ArrowUpRight, Banknote, Clock3, HandCoins, PiggyBank, ReceiptText } from 'lucide-react';
import { Area, AreaChart, Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import EmptyState from '../components/EmptyState';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import MetricCard from '../components/MetricCard';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import StatusBadge from '../components/StatusBadge';
import { api } from '../services/api';
import { formatDate, formatMoney } from '../utils/format';

import { useMemo } from 'react';

export default function DashboardPage() {
  const dashboard = useQuery({ queryKey: ['dashboard'], queryFn: api.dashboard });
  const loans = useQuery({ queryKey: ['loans'], queryFn: () => api.loans() });
  const savings = useQuery({ queryKey: ['savings'], queryFn: api.savings });

  const loanStats = useMemo(() => {
    if (!loans.data) return [];
    const statusCounts: Record<string, number> = { PENDING: 0, ACTIVE: 0, REPAID: 0, DEFAULTED: 0, REJECTED: 0, CANCELLED: 0 };
    loans.data.forEach((loan: any) => {
      if (statusCounts[loan.status] !== undefined) {
        statusCounts[loan.status]++;
      } else {
        statusCounts[loan.status] = 1;
      }
    });
    return [
      { name: 'Pending', value: statusCounts.PENDING },
      { name: 'Active', value: statusCounts.ACTIVE },
      { name: 'Repaid', value: statusCounts.REPAID },
      { name: 'Defaulted', value: statusCounts.DEFAULTED },
    ];
  }, [loans.data]);

  const savingsTrend = useMemo(() => {
    if (!savings.data) return [];
    const sorted = [...savings.data].sort((a: any, b: any) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    
    const grouped: Record<string, number> = {};
    sorted.forEach((saving: any) => {
      const date = new Date(saving.createdAt);
      const monthStr = months[date.getMonth()];
      if (!grouped[monthStr]) grouped[monthStr] = 0;
      grouped[monthStr] += saving.amount;
    });

    const trend: Array<{month: string, savings: number}> = [];
    let currentTotal = 0;
    
    months.forEach((month, index) => {
      if (grouped[month] !== undefined) {
        currentTotal += grouped[month];
        trend.push({ month, savings: currentTotal });
      } else if (trend.length > 0) {
        trend.push({ month, savings: currentTotal });
      } else {
        trend.push({ month, savings: 0 });
      }
    });
    return trend.slice(new Date().getMonth() - 5, new Date().getMonth() + 1);
  }, [savings.data]);

  if (dashboard.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Finance operations"
        title="Dashboard"
        description="Live view of cooperative savings, loan exposure, repayments, and member activity."
        actions={
          <button className="button primary">
            <ArrowUpRight size={15} />
            Quick report
          </button>
        }
      />

      <ErrorBanner message={dashboard.error?.message || loans.error?.message || savings.error?.message} />

      <section className="metrics-grid">
        <MetricCard title="Total Savings" value={dashboard.data?.totalSavings ?? 0} icon={PiggyBank} trend="+12.4% cooperative capital" />
        <MetricCard title="Active Loans" value={dashboard.data?.outstandingLoans ?? 0} icon={HandCoins} tone="cyan" trend="Outstanding portfolio" />
        <MetricCard title="Monthly Income" value={dashboard.data?.expectedInterest ?? 0} icon={Banknote} tone="emerald" trend="Expected interest income" />
        <MetricCard title="Pending Repayments" value={dashboard.data?.pendingLoans ?? 0} icon={Clock3} money={false} tone="amber" trend="Awaiting committee review" />
      </section>

      <section className="dashboard-grid">
        <Panel title="Savings growth" description="Monthly cooperative capital growth">
          <div className="chart-box">
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={savingsTrend}>
                <defs>
                  <linearGradient id="savingsGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0F766E" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#0F766E" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                <XAxis dataKey="month" tickLine={false} axisLine={false} fontSize={11} />
                <YAxis tickLine={false} axisLine={false} fontSize={11} tickFormatter={(value) => `${value / 1000}k`} />
                <Tooltip formatter={(value) => formatMoney(Number(value))} />
                <Area type="monotone" dataKey="savings" stroke="#0F766E" fill="url(#savingsGradient)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Panel>

        <Panel title="Loan repayment statistics" description="Portfolio status distribution">
          <div className="chart-box">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={loanStats}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
                <XAxis dataKey="name" tickLine={false} axisLine={false} fontSize={11} />
                <YAxis tickLine={false} axisLine={false} fontSize={11} />
                <Tooltip />
                <Bar dataKey="value" fill="#06B6D4" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Panel>
      </section>

      <section className="dashboard-grid">
        <Panel title="Recent loans" description="Latest requests and portfolio movements">
          {loans.data?.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Amount</th>
                    <th>Installment</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {loans.data.slice(0, 6).map((loan) => (
                    <tr key={loan.id}>
                      <td>{loan.member.fullName}</td>
                      <td>{formatMoney(loan.amount)}</td>
                      <td>{formatMoney(loan.monthlyInstallment)}</td>
                      <td>
                        <StatusBadge value={loan.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState icon={ReceiptText} title="No loans yet" message="Loan requests will appear here once members start applying." />
          )}
        </Panel>

        <Panel title="Recent transactions" description="Savings and repayment activity">
          {savings.data?.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Date</th>
                    <th>Member</th>
                    <th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {savings.data.slice(0, 6).map((saving) => (
                    <tr key={saving.id}>
                      <td>Saving</td>
                      <td>{formatDate(saving.createdAt)}</td>
                      <td>{saving.member.fullName}</td>
                      <td>{formatMoney(saving.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState icon={PiggyBank} title="No savings yet" message="Monthly savings deductions will appear in this table." />
          )}
        </Panel>
      </section>
    </div>
  );
}
