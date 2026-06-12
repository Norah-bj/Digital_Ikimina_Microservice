import { ChevronDown, ChevronLeft, ChevronRight, ChevronsUpDown, Columns3, Search, SlidersHorizontal, type LucideIcon } from 'lucide-react';
import { ReactNode, useMemo, useState } from 'react';
import EmptyState from './EmptyState';
import LoadingState from './LoadingState';

export interface DataColumn<T> {
  key: string;
  header: string;
  accessor: (row: T) => string | number | boolean | null | undefined;
  render?: (row: T) => ReactNode;
  sortable?: boolean;
  filterable?: boolean;
  hidden?: boolean;
}

interface DataTableProps<T> {
  data: T[];
  columns: DataColumn<T>[];
  getRowKey: (row: T) => string | number;
  searchPlaceholder?: string;
  emptyTitle: string;
  emptyMessage: string;
  emptyIcon: LucideIcon;
  loading?: boolean;
  actions?: ReactNode;
}

const pageSizes = [5, 10, 20, 50];

function normalize(value: unknown) {
  return String(value ?? '').toLowerCase();
}

export default function DataTable<T>({
  data,
  columns,
  getRowKey,
  searchPlaceholder = 'Search records...',
  emptyTitle,
  emptyMessage,
  emptyIcon,
  loading,
  actions,
}: DataTableProps<T>) {
  const [search, setSearch] = useState('');
  const [sortKey, setSortKey] = useState(columns.find((column) => column.sortable)?.key ?? columns[0]?.key ?? '');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [visibleColumns, setVisibleColumns] = useState(() =>
    Object.fromEntries(columns.map((column) => [column.key, !column.hidden])) as Record<string, boolean>,
  );
  const [columnMenuOpen, setColumnMenuOpen] = useState(false);

  const activeColumns = columns.filter((column) => visibleColumns[column.key]);

  const filtered = useMemo(() => {
    const searchable = columns.filter((column) => column.filterable !== false);
    const next = data.filter((row) => searchable.some((column) => normalize(column.accessor(row)).includes(search.toLowerCase())));
    const sortColumn = columns.find((column) => column.key === sortKey);
    if (!sortColumn) return next;
    return [...next].sort((a, b) => {
      const left = sortColumn.accessor(a);
      const right = sortColumn.accessor(b);
      const leftNumber = Number(left);
      const rightNumber = Number(right);
      const result =
        Number.isFinite(leftNumber) && Number.isFinite(rightNumber)
          ? leftNumber - rightNumber
          : String(left ?? '').localeCompare(String(right ?? ''));
      return sortDirection === 'asc' ? result : -result;
    });
  }, [columns, data, search, sortDirection, sortKey]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / rowsPerPage));
  const safePage = Math.min(page, totalPages);
  const start = (safePage - 1) * rowsPerPage;
  const pageRows = filtered.slice(start, start + rowsPerPage);

  function sort(column: DataColumn<T>) {
    if (!column.sortable) return;
    if (sortKey === column.key) {
      setSortDirection((current) => (current === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(column.key);
      setSortDirection('asc');
    }
    setPage(1);
  }

  if (loading) return <LoadingState />;

  return (
    <div className="data-table">
      <div className="table-toolbar">
        <label className="search-input">
          <Search size={14} />
          <input
            value={search}
            onChange={(event) => {
              setSearch(event.target.value);
              setPage(1);
            }}
            placeholder={searchPlaceholder}
            aria-label={searchPlaceholder}
          />
        </label>
        <div className="table-toolbar__actions">
          {actions}
          <button className="button secondary" type="button" onClick={() => setColumnMenuOpen((value) => !value)}>
            <Columns3 size={14} />
            Columns
            <ChevronDown size={13} />
          </button>
          {columnMenuOpen && (
            <div className="column-menu">
              {columns.map((column) => (
                <label key={column.key}>
                  <input
                    type="checkbox"
                    checked={visibleColumns[column.key]}
                    onChange={(event) => setVisibleColumns((current) => ({ ...current, [column.key]: event.target.checked }))}
                  />
                  <span>{column.header}</span>
                </label>
              ))}
            </div>
          )}
        </div>
      </div>

      {pageRows.length ? (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  {activeColumns.map((column) => (
                    <th key={column.key}>
                      <button
                        className="th-button"
                        type="button"
                        onClick={() => sort(column)}
                        disabled={!column.sortable}
                        aria-label={column.sortable ? `Sort by ${column.header}` : undefined}
                      >
                        {column.header}
                        {column.sortable && <ChevronsUpDown size={12} />}
                      </button>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {pageRows.map((row) => (
                  <tr key={getRowKey(row)}>
                    {activeColumns.map((column) => (
                      <td key={column.key}>{column.render ? column.render(row) : column.accessor(row)}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="table-pagination">
            <span>
              Showing {start + 1}-{Math.min(start + rowsPerPage, filtered.length)} of {filtered.length}
            </span>
            <label>
              <SlidersHorizontal size={13} />
              <select
                value={rowsPerPage}
                onChange={(event) => {
                  setRowsPerPage(Number(event.target.value));
                  setPage(1);
                }}
                aria-label="Rows per page"
              >
                {pageSizes.map((size) => (
                  <option key={size} value={size}>
                    {size} rows
                  </option>
                ))}
              </select>
            </label>
            <div className="page-buttons">
              <button className="icon-button" type="button" onClick={() => setPage((value) => Math.max(1, value - 1))} disabled={safePage === 1} aria-label="Previous page">
                <ChevronLeft size={15} />
              </button>
              {Array.from({ length: totalPages }, (_, index) => index + 1).slice(0, 5).map((item) => (
                <button key={item} className={`page-number ${safePage === item ? 'active' : ''}`} type="button" onClick={() => setPage(item)}>
                  {item}
                </button>
              ))}
              <button className="icon-button" type="button" onClick={() => setPage((value) => Math.min(totalPages, value + 1))} disabled={safePage === totalPages} aria-label="Next page">
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        </>
      ) : (
        <EmptyState icon={emptyIcon} title={emptyTitle} message={emptyMessage} />
      )}
    </div>
  );
}
