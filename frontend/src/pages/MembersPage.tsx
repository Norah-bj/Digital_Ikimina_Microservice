import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Eye, Pencil, Plus, UserPlus, UsersRound } from 'lucide-react';
import { useState } from 'react';
import DataTable, { type DataColumn } from '../components/DataTable';
import ErrorBanner from '../components/ErrorBanner';
import LoadingState from '../components/LoadingState';
import MemberForm from '../components/MemberForm';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import Panel from '../components/Panel';
import StatusBadge from '../components/StatusBadge';
import { useToast } from '../components/ToastProvider';
import { api } from '../services/api';
import type { Member, MemberPayload } from '../types';
import { formatDate, formatMoney } from '../utils/format';

export default function MembersPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [dialog, setDialog] = useState<'add' | 'edit' | 'view' | null>(null);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const members = useQuery({ queryKey: ['members'], queryFn: api.members });
  const createMember = useMutation({
    mutationFn: api.createMember,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] });
      setDialog(null);
      showToast('Member created successfully.', 'success');
    },
  });
  const updateMember = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: MemberPayload }) => api.updateMember(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] });
      setDialog(null);
      showToast('Member updated successfully.', 'success');
    },
  });

  const columns: DataColumn<Member>[] = [
    {
      key: 'name',
      header: 'Name',
      accessor: (member) => member.fullName,
      sortable: true,
      render: (member) => (
        <button className="table-link button-link" type="button" onClick={() => openDialog('view', member)}>
          {member.fullName}
          <small>{member.email}</small>
        </button>
      ),
    },
    { key: 'role', header: 'Role', accessor: (member) => member.role.replaceAll('_', ' '), sortable: true },
    { key: 'contribution', header: 'Contribution', accessor: (member) => member.contributionPercentage, sortable: true, render: (member) => `${member.contributionPercentage}%` },
    { key: 'salary', header: 'Salary', accessor: (member) => member.monthlySalary, sortable: true, render: (member) => formatMoney(member.monthlySalary) },
    { key: 'joined', header: 'Joined', accessor: (member) => member.joinDate, sortable: true, render: (member) => formatDate(member.joinDate) },
    { key: 'status', header: 'Status', accessor: (member) => (member.isActive ? 'ACTIVE' : 'INACTIVE'), sortable: true, render: (member) => <StatusBadge value={member.isActive ? 'ACTIVE' : 'INACTIVE'} /> },
    {
      key: 'actions',
      header: 'Actions',
      accessor: () => '',
      filterable: false,
      render: (member) => (
        <div className="row-actions">
          <button className="icon-button" type="button" onClick={() => openDialog('view', member)} aria-label={`View ${member.fullName}`}>
            <Eye size={14} />
          </button>
          <button className="icon-button" type="button" onClick={() => openDialog('edit', member)} aria-label={`Edit ${member.fullName}`}>
            <Pencil size={14} />
          </button>
        </div>
      ),
    },
  ];

  function openDialog(nextDialog: 'add' | 'edit' | 'view', member?: Member) {
    setSelectedMember(member ?? null);
    setDialog(nextDialog);
  }

  if (members.isLoading) return <LoadingState />;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Membership"
        title="Members"
        description="Manage government worker profiles, contribution settings, roles, and active status."
        actions={
          <button className="button primary" type="button" onClick={() => openDialog('add')}>
            <Plus size={15} />
            Add member
          </button>
        }
      />
      <ErrorBanner message={members.error?.message || createMember.error?.message || updateMember.error?.message} />

      <Panel
        title="Member directory"
        description="Search, sort, filter columns, and inspect cooperative participants"
        actions={
          <span className="mini-stat">
            <UserPlus size={14} />
            {members.data?.length ?? 0} members
          </span>
        }
      >
        <DataTable
          data={members.data ?? []}
          columns={columns}
          getRowKey={(member) => member.id}
          searchPlaceholder="Search by name, email, role, status..."
          emptyIcon={UsersRound}
          emptyTitle="No members found"
          emptyMessage="Add a member to start building the cooperative directory."
        />
      </Panel>

      <Modal open={dialog === 'add'} title="Add member" description="Create a cooperative member profile." onClose={() => setDialog(null)}>
        <MemberForm onSubmit={(payload) => createMember.mutate(payload)} busy={createMember.isPending} />
      </Modal>

      <Modal open={dialog === 'edit' && Boolean(selectedMember)} title="Edit member" description="Update member profile information." onClose={() => setDialog(null)}>
        {selectedMember && (
          <MemberForm
            initialValue={selectedMember}
            submitLabel="Save changes"
            onSubmit={(payload) => updateMember.mutate({ id: selectedMember.id, payload })}
            busy={updateMember.isPending}
          />
        )}
      </Modal>

      <Modal open={dialog === 'view' && Boolean(selectedMember)} title="Member details" description="Profile, role, contribution, and current status." onClose={() => setDialog(null)}>
        {selectedMember && (
          <div className="detail-list">
            <div><span>Name</span><strong>{selectedMember.fullName}</strong></div>
            <div><span>Email</span><strong>{selectedMember.email}</strong></div>
            <div><span>Phone</span><strong>{selectedMember.phone || 'Not provided'}</strong></div>
            <div><span>National ID</span><strong>{selectedMember.nationalId || 'Not provided'}</strong></div>
            <div><span>Monthly salary</span><strong>{formatMoney(selectedMember.monthlySalary)}</strong></div>
            <div><span>Contribution</span><strong>{selectedMember.contributionPercentage}%</strong></div>
            <div><span>Joined</span><strong>{formatDate(selectedMember.joinDate)}</strong></div>
            <div><span>Status</span><StatusBadge value={selectedMember.isActive ? 'ACTIVE' : 'INACTIVE'} /></div>
          </div>
        )}
      </Modal>
    </div>
  );
}
