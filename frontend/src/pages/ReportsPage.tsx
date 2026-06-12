import { useQuery } from '@tanstack/react-query';
import { Download, FileSpreadsheet, FileText } from 'lucide-react';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import MetricCard from '../components/MetricCard';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import { api } from '../services/api';
import { formatMoney } from '../utils/format';

export default function ReportsPage() {
  const dashboard = useQuery({ queryKey: ['dashboard'], queryFn: api.dashboard });

  if (dashboard.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Analytics"
        title="Reports"
        description="Financial statements, default analysis, loan trends, and export-ready summaries."
        actions={
          <div className="button-row">
            <button className="button secondary"><FileText size={15} /> PDF</button>
            <button className="button secondary"><FileSpreadsheet size={15} /> Excel</button>
          </div>
        }
      />
      <ErrorBanner message={dashboard.error?.message} />
      <section className="metrics-grid">
        <MetricCard title="Total profits" value={dashboard.data?.expectedInterest ?? 0} icon={Download} />
        <MetricCard title="Monthly cash flow" value={(dashboard.data?.totalSavings ?? 0) + (dashboard.data?.totalRepayments ?? 0)} icon={FileSpreadsheet} tone="cyan" />
        <MetricCard title="Default exposure" value={dashboard.data?.outstandingLoans ?? 0} icon={FileText} tone="amber" />
        <MetricCard title="Total repayments" value={dashboard.data?.totalRepayments ?? 0} icon={Download} tone="emerald" />
      </section>
      <Panel title="Executive summary" description="High-level financial interpretation">
        <div className="insight-grid">
          <div><span>Capital base</span><strong>{formatMoney(dashboard.data?.totalSavings)}</strong><p>Total member savings available for cooperative operations.</p></div>
          <div><span>Loan book</span><strong>{formatMoney(dashboard.data?.outstandingLoans)}</strong><p>Outstanding exposure across active and defaulted loans.</p></div>
          <div><span>Income engine</span><strong>{formatMoney(dashboard.data?.expectedInterest)}</strong><p>Expected 6% interest income from loan activity.</p></div>
        </div>
      </Panel>
    </div>
  );
}
