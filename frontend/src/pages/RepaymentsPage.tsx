import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CircleDollarSign, Plus } from 'lucide-react';
import { useState } from 'react';
import DataTable, { type DataColumn } from '../components/DataTable';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import RepaymentForm from '../components/RepaymentForm';
import StatusBadge from '../components/StatusBadge';
import { useToast } from '../components/ToastProvider';
import { api } from '../services/api';
import type { Repayment, RepaymentPayload } from '../types';
import { formatDate, formatMoney } from '../utils/format';

export default function RepaymentsPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [addOpen, setAddOpen] = useState(false);
  const loans = useQuery({ queryKey: ['loans'], queryFn: () => api.loans() });
  const activeLoanId = loans.data?.find((loan) => loan.status === 'ACTIVE' || loan.status === 'DEFAULTED')?.id;
  const repayments = useQuery({
    queryKey: ['repayments', activeLoanId],
    queryFn: () => api.repayments(activeLoanId!),
    enabled: Boolean(activeLoanId),
  });
  const createRepayment = useMutation({
    mutationFn: ({ loanId, payload }: { loanId: number; payload: RepaymentPayload }) => api.createRepayment(loanId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['loans'] });
      queryClient.invalidateQueries({ queryKey: ['repayments'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      setAddOpen(false);
      showToast('Repayment recorded successfully.', 'success');
    },
  });

  const columns: DataColumn<Repayment>[] = [
    { key: 'loan', header: 'Loan', accessor: (repayment) => repayment.loan.id, sortable: true, render: (repayment) => `#${repayment.loan.id}` },
    { key: 'date', header: 'Date', accessor: (repayment) => repayment.paymentDate, sortable: true, render: (repayment) => formatDate(repayment.paymentDate) },
    { key: 'amount', header: 'Amount', accessor: (repayment) => repayment.amount, sortable: true, render: (repayment) => formatMoney(repayment.amount) },
    { key: 'balance', header: 'Loan balance', accessor: (repayment) => repayment.remainingBalance, sortable: true, render: (repayment) => formatMoney(repayment.remainingBalance) },
    { key: 'status', header: 'Status', accessor: (repayment) => repayment.status, sortable: true, render: (repayment) => <StatusBadge value={repayment.status} /> },
    { key: 'note', header: 'Note', accessor: (repayment) => repayment.note ?? '', sortable: true },
  ];

  if (loans.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Repayments"
        title="Salary deductions"
        description="Post repayment records and monitor remaining balances."
        actions={
          <button className="button primary" type="button" onClick={() => setAddOpen(true)}>
            <Plus size={15} />
            Add repayment
          </button>
        }
      />
      <ErrorBanner message={loans.error?.message || repayments.error?.message || createRepayment.error?.message} />
      <Panel title="Repayment view" description="Showing repayments for the first active loan">
        <DataTable
          data={repayments.data ?? []}
          columns={columns}
          getRowKey={(repayment) => repayment.id}
          searchPlaceholder="Search repayments by loan, date, amount, status..."
          emptyIcon={CircleDollarSign}
          emptyTitle="No repayment records"
          emptyMessage="Approve a loan, then use Add repayment to post salary deductions."
          loading={repayments.isLoading}
        />
      </Panel>
      <Modal open={addOpen} title="Add repayment" description="Only active or defaulted loans accept repayment." onClose={() => setAddOpen(false)}>
        <RepaymentForm loans={loans.data ?? []} onSubmit={(loanId, payload) => createRepayment.mutate({ loanId, payload })} busy={createRepayment.isPending} />
      </Modal>
    </div>
  );
}
