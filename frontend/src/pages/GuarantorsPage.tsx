import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Eye, Plus, ShieldCheck } from 'lucide-react';
import { useState } from 'react';
import DataTable, { type DataColumn } from '../components/DataTable';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import LoanForm from '../components/LoanForm';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import StatusBadge from '../components/StatusBadge';
import { useToast } from '../components/ToastProvider';
import { api } from '../services/api';
import type { Loan } from '../types';
import { formatDate, formatMoney } from '../utils/format';

export default function GuarantorsPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [addOpen, setAddOpen] = useState(false);
  const [selectedLoan, setSelectedLoan] = useState<Loan | null>(null);
  const loans = useQuery({ queryKey: ['loans'], queryFn: () => api.loans() });
  const members = useQuery({ queryKey: ['members'], queryFn: api.members });
  const createLoan = useMutation({
    mutationFn: api.createLoan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loans'] });
      setAddOpen(false);
      showToast('Guaranteed loan request created successfully.', 'success');
    },
  });
  const guaranteedLoans = (loans.data ?? []).filter((loan) => loan.guarantor);

  const columns: DataColumn<Loan>[] = [
    { key: 'name', header: 'Name', accessor: (loan) => loan.member.fullName, sortable: true },
    { key: 'guarantor', header: 'Guarantor', accessor: (loan) => loan.guarantor?.fullName ?? '', sortable: true },
    { key: 'date', header: 'Date', accessor: (loan) => loan.requestDate, sortable: true, render: (loan) => formatDate(loan.requestDate) },
    { key: 'amount', header: 'Amount', accessor: (loan) => loan.guaranteeAmount ?? loan.amount, sortable: true, render: (loan) => formatMoney(loan.guaranteeAmount ?? loan.amount) },
    { key: 'status', header: 'Status', accessor: (loan) => loan.guaranteeStatus, sortable: true, render: (loan) => <StatusBadge value={loan.guaranteeStatus} /> },
    {
      key: 'actions',
      header: 'Actions',
      accessor: () => '',
      filterable: false,
      render: (loan) => (
        <button className="icon-button" type="button" onClick={() => setSelectedLoan(loan)} aria-label={`View guarantor for loan ${loan.id}`}>
          <Eye size={14} />
        </button>
      ),
    },
  ];

  if (loans.isLoading || members.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Trust network"
        title="Guarantors"
        description="Visualize who guarantees whom and the active liability behind each relationship."
        actions={
          <button className="button primary" type="button" onClick={() => setAddOpen(true)}>
            <Plus size={15} />
            Add guarantor
          </button>
        }
      />
      <ErrorBanner message={loans.error?.message || members.error?.message || createLoan.error?.message} />
      <Panel title="Guarantee relationships" description="Member A guarantees Member B when requested amount exceeds savings">
        <DataTable
          data={guaranteedLoans}
          columns={columns}
          getRowKey={(loan) => loan.id}
          searchPlaceholder="Search guarantors by name, borrower, status..."
          emptyIcon={ShieldCheck}
          emptyTitle="No guarantees active"
          emptyMessage="Guaranteed loans will create relationship records here."
        />
      </Panel>
      <Modal open={addOpen} title="Add guarantor" description="Create a loan request and attach guarantor details through the existing workflow." onClose={() => setAddOpen(false)}>
        <LoanForm members={members.data ?? []} onSubmit={(payload) => createLoan.mutate(payload)} busy={createLoan.isPending} />
      </Modal>
      <Modal open={Boolean(selectedLoan)} title="Guarantor details" description="Borrower, guarantor, exposure, and current guarantee status." onClose={() => setSelectedLoan(null)}>
        {selectedLoan && (
          <div className="detail-list">
            <div><span>Guarantor</span><strong>{selectedLoan.guarantor?.fullName}</strong></div>
            <div><span>Borrower</span><strong>{selectedLoan.member.fullName}</strong></div>
            <div><span>Guarantee amount</span><strong>{formatMoney(selectedLoan.guaranteeAmount ?? selectedLoan.amount)}</strong></div>
            <div><span>Loan amount</span><strong>{formatMoney(selectedLoan.amount)}</strong></div>
            <div><span>Loan balance</span><strong>{formatMoney(selectedLoan.outstandingBalance)}</strong></div>
            <div><span>Request date</span><strong>{formatDate(selectedLoan.requestDate)}</strong></div>
            <div><span>Status</span><StatusBadge value={selectedLoan.guaranteeStatus} /></div>
          </div>
        )}
      </Modal>
    </div>
  );
}
