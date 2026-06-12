import { Building2, ShieldCheck } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import StatusBadge from '../components/StatusBadge';

const admins = [
  { name: 'Director General', role: 'SUPER_ADMIN', scope: 'Full platform control' },
  { name: 'Accountant Admin', role: 'ACCOUNTANT_ADMIN', scope: 'Reports and deductions' },
  { name: 'Loan Committee', role: 'LOAN_COMMITTEE', scope: 'Loan approvals' },
  { name: 'Secretary', role: 'SECRETARY', scope: 'Members and notices' },
];

export default function AdminsPage() {
  return (
    <div className="page-stack">
      <PageHeader eyebrow="Governance" title="Admins" description="Role-based operating model for cooperative leadership and finance teams." />
      <Panel title="Administrative roles" description="Permission structure for the Ikimina platform">
        <div className="admin-grid">
          {admins.map((admin) => (
            <article className="admin-card" key={admin.role}>
              <span className="icon-chip emerald"><ShieldCheck size={16} /></span>
              <strong>{admin.name}</strong>
              <p>{admin.scope}</p>
              <StatusBadge value={admin.role} />
            </article>
          ))}
        </div>
      </Panel>
      <Panel title="Institution profile" description="Government worker cooperative configuration">
        <div className="institution-card">
          <Building2 size={28} />
          <div><strong>Digital Ikimina Cooperative</strong><span>Rwanda public sector savings and lending platform</span></div>
        </div>
      </Panel>
    </div>
  );
}
