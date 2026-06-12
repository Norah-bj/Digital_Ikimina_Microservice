import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { PiggyBank, Plus } from 'lucide-react';
import { useState } from 'react';
import DataTable, { type DataColumn } from '../components/DataTable';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import SavingForm from '../components/SavingForm';
import { useToast } from '../components/ToastProvider';
import { api } from '../services/api';
import type { Saving } from '../types';
import { formatDate, formatMoney } from '../utils/format';

export default function SavingsPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [addOpen, setAddOpen] = useState(false);
  const savings = useQuery({ queryKey: ['savings'], queryFn: api.savings });
  const members = useQuery({ queryKey: ['members'], queryFn: api.members });
  const createSaving = useMutation({
    mutationFn: api.createSaving,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['savings'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      setAddOpen(false);
      showToast('Saving recorded successfully.', 'success');
    },
  });

  const columns: DataColumn<Saving>[] = [
    { key: 'name', header: 'Name', accessor: (saving) => saving.member.fullName, sortable: true },
    { key: 'month', header: 'Month', accessor: (saving) => saving.savingMonth, sortable: true },
    { key: 'date', header: 'Date', accessor: (saving) => saving.createdAt, sortable: true, render: (saving) => formatDate(saving.createdAt) },
    { key: 'amount', header: 'Savings amount', accessor: (saving) => saving.amount, sortable: true, render: (saving) => formatMoney(saving.amount) },
    { key: 'role', header: 'Role', accessor: (saving) => saving.member.role.replaceAll('_', ' '), sortable: true, hidden: true },
  ];

  if (savings.isLoading || members.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Savings"
        title="Monthly savings"
        description="Record salary deductions and track contribution history."
        actions={
          <button className="button primary" type="button" onClick={() => setAddOpen(true)}>
            <Plus size={15} />
            Add saving
          </button>
        }
      />
      <ErrorBanner message={savings.error?.message || members.error?.message || createSaving.error?.message} />
      <Panel title="Savings ledger" description="All recorded member contributions">
        <DataTable
          data={savings.data ?? []}
          columns={columns}
          getRowKey={(saving) => saving.id}
          searchPlaceholder="Search savings by member, month, amount..."
          emptyIcon={PiggyBank}
          emptyTitle="No savings recorded"
          emptyMessage="Use Add saving to record the first monthly contribution."
        />
      </Panel>
      <Modal open={addOpen} title="Add saving" description="Minimum contribution is 5,000 RWF." onClose={() => setAddOpen(false)}>
        <SavingForm members={members.data ?? []} onSubmit={(payload) => createSaving.mutate(payload)} busy={createSaving.isPending} />
      </Modal>
    </div>
  );
}
