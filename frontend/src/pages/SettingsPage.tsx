import { Save, Settings } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';

export default function SettingsPage() {
  return (
    <div className="page-stack">
      <PageHeader eyebrow="Configuration" title="Settings" description="System rules for savings, interest, approval limits, and notification channels." />
      <Panel title="Financial rules" description="Configuration mirrors the current backend rules">
        <form className="form-grid">
          <label><span>Minimum monthly saving</span><input value="5000 RWF" readOnly /></label>
          <label><span>Loan interest rate</span><input value="6%" readOnly /></label>
          <label><span>Membership loan waiting period</span><input value="3 months" readOnly /></label>
          <label><span>Salary installment limit</span><input value="60%" readOnly /></label>
          <button className="button primary form-submit" type="button"><Save size={15} /> Save settings</button>
        </form>
      </Panel>
      <Panel title="System modules" description="Core backend modules connected to the frontend">
        <div className="module-grid">
          {['Members', 'Savings', 'Loans', 'Repayments', 'Guarantors', 'Reports'].map((item) => (
            <div className="module-card" key={item}><Settings size={15} /><strong>{item}</strong><span>Enabled</span></div>
          ))}
        </div>
      </Panel>
    </div>
  );
}
