import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckCircle2, Eye, HandCoins, Pencil, Plus, XCircle } from 'lucide-react';
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

export default function LoansPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [dialog, setDialog] = useState<'add' | 'details' | 'edit' | null>(null);
  const [selectedLoan, setSelectedLoan] = useState<Loan | null>(null);
  const loans = useQuery({ queryKey: ['loans'], queryFn: () => api.loans() });
  const members = useQuery({ queryKey: ['members'], queryFn: api.members });
  const createLoan = useMutation({
    mutationFn: api.createLoan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loans'] });
      setDialog(null);
      showToast('Loan request created successfully.', 'success');
    },
  });
  const approveLoan = useMutation({
    mutationFn: api.approveLoan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loans'] });
      showToast('Loan approved successfully.', 'success');
    },
  });
  const rejectLoan = useMutation({
    mutationFn: (id: number) => api.rejectLoan(id, 'Rejected from dashboard review'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loans'] });
      showToast('Loan rejected.', 'warning');
    },
  });

  const columns: DataColumn<Loan>[] = [
    { key: 'name', header: 'Name', accessor: (loan) => loan.member.fullName, sortable: true },
    { key: 'date', header: 'Date', accessor: (loan) => loan.requestDate, sortable: true, render: (loan) => formatDate(loan.requestDate) },
    { key: 'amount', header: 'Amount', accessor: (loan) => loan.amount, sortable: true, render: (loan) => formatMoney(loan.amount) },
    { key: 'balance', header: 'Loan balance', accessor: (loan) => loan.outstandingBalance, sortable: true, render: (loan) => formatMoney(loan.outstandingBalance) },
    { key: 'installment', header: 'Installment', accessor: (loan) => loan.monthlyInstallment, sortable: true, render: (loan) => formatMoney(loan.monthlyInstallment) },
    { key: 'status', header: 'Status', accessor: (loan) => loan.status, sortable: true, render: (loan) => <StatusBadge value={loan.status} /> },
    {
      key: 'actions',
      header: 'Actions',
      accessor: () => '',
      filterable: false,
      render: (loan) => (
        <div className="row-actions">
          <button className="icon-button" type="button" onClick={() => openDialog('details', loan)} aria-label={`View loan ${loan.id}`}>
            <Eye size={14} />
          </button>
          <button className="icon-button" type="button" onClick={() => openDialog('edit', loan)} aria-label={`Edit loan ${loan.id}`}>
            <Pencil size={14} />
          </button>
          {loan.status === 'PENDING' && (
            <>
              <button className="icon-button success" type="button" onClick={() => approveLoan.mutate(loan.id)} aria-label={`Approve loan ${loan.id}`}>
                <CheckCircle2 size={14} />
              </button>
              <button className="icon-button danger" type="button" onClick={() => rejectLoan.mutate(loan.id)} aria-label={`Reject loan ${loan.id}`}>
                <XCircle size={14} />
              </button>
            </>
          )}
        </div>
      ),
    },
  ];

  function openDialog(nextDialog: 'add' | 'details' | 'edit', loan?: Loan) {
    setSelectedLoan(loan ?? null);
    setDialog(nextDialog);
  }

  if (loans.isLoading || members.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Credit operations"
        title="Loans"
        description="Review loan requests, guarantors, installments, and repayment exposure."
        actions={
          <button className="button primary" type="button" onClick={() => openDialog('add')}>
            <Plus size={15} />
            Add loan
          </button>
        }
      />
      <ErrorBanner message={loans.error?.message || members.error?.message || createLoan.error?.message || approveLoan.error?.message || rejectLoan.error?.message} />

      <Panel title="Loan portfolio" description="Committee review queue, balances, guarantors, and active loans">
        <DataTable
          data={loans.data ?? []}
          columns={columns}
          getRowKey={(loan) => loan.id}
          searchPlaceholder="Search loans by member, status, amount..."
          emptyIcon={HandCoins}
          emptyTitle="No loans yet"
          emptyMessage="Use Add loan to create the first loan application."
        />
      </Panel>

      <Modal open={dialog === 'add'} title="Add loan" description="The system applies savings, guarantor, and salary rules." onClose={() => setDialog(null)}>
        <LoanForm members={members.data ?? []} onSubmit={(payload) => createLoan.mutate(payload)} busy={createLoan.isPending} />
      </Modal>

      <Modal open={(dialog === 'details' || dialog === 'edit') && Boolean(selectedLoan)} title={dialog === 'edit' ? 'Edit loan' : 'Loan details'} description="Existing loan rules and calculated values are shown without changing backend financial logic." onClose={() => setDialog(null)}>
        {selectedLoan && (
          <div className="detail-list">
            <div><span>Borrower</span><strong>{selectedLoan.member.fullName}</strong></div>
            <div><span>Purpose</span><strong>{selectedLoan.purpose || 'General support'}</strong></div>
            <div><span>Requested amount</span><strong>{formatMoney(selectedLoan.amount)}</strong></div>
            <div><span>Outstanding balance</span><strong>{formatMoney(selectedLoan.outstandingBalance)}</strong></div>
            <div><span>Total payable</span><strong>{formatMoney(selectedLoan.totalPayable)}</strong></div>
            <div><span>Monthly installment</span><strong>{formatMoney(selectedLoan.monthlyInstallment)}</strong></div>
            <div><span>Request date</span><strong>{formatDate(selectedLoan.requestDate)}</strong></div>
            <div><span>Due date</span><strong>{formatDate(selectedLoan.dueDate)}</strong></div>
            <div><span>Guarantor</span><strong>{selectedLoan.guarantor?.fullName ?? selectedLoan.guaranteeStatus.replaceAll('_', ' ')}</strong></div>
            <div><span>Status</span><StatusBadge value={selectedLoan.status} /></div>
            {selectedLoan.status === 'PENDING' && (
              <div className="button-row span-2">
                <button className="button primary" type="button" onClick={() => approveLoan.mutate(selectedLoan.id)}>
                  <CheckCircle2 size={14} />
                  Approve
                </button>
                <button className="button danger" type="button" onClick={() => rejectLoan.mutate(selectedLoan.id)}>
                  <XCircle size={14} />
                  Reject
                </button>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
