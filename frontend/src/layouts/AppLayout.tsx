import {
  Bell,
  Building2,
  ChartNoAxesCombined,
  ChevronDown,
  CircleDollarSign,
  FileBarChart,
  HandCoins,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageSquare,
  PiggyBank,
  Search,
  Settings,
  ShieldCheck,
  UserRound,
  UsersRound,
  WalletCards,
  X,
} from 'lucide-react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useToast } from '../components/ToastProvider';
import { useUiStore } from '../store/uiStore';

const navItems = [
  { label: 'Dashboard', path: '/', icon: LayoutDashboard },
  { label: 'Members', path: '/members', icon: UsersRound },
  { label: 'Savings', path: '/savings', icon: PiggyBank },
  { label: 'Loans', path: '/loans', icon: HandCoins },
  { label: 'Repayments', path: '/repayments', icon: CircleDollarSign },
  { label: 'Guarantors', path: '/guarantors', icon: ShieldCheck },
  { label: 'Shares', path: '/shares', icon: WalletCards },
  { label: 'Reports', path: '/reports', icon: FileBarChart },
  { label: 'Notifications', path: '/notifications', icon: MessageSquare },
  { label: 'Settings', path: '/settings', icon: Settings },
  { label: 'Admins', path: '/admins', icon: Building2 },
];

export default function AppLayout() {
  const { sidebarOpen, toggleSidebar, closeSidebar } = useUiStore();
  const location = useLocation();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const date = new Intl.DateTimeFormat('en-RW', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date());

  return (
    <div className="app-shell">
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar__brand">
          <span className="brand-mark">
            <ChartNoAxesCombined size={18} />
          </span>
          <div>
            <strong>Digital Ikimina</strong>
            <small>Cooperative finance</small>
          </div>
          <button className="icon-button mobile-only" onClick={closeSidebar} aria-label="Close menu">
            <X size={16} />
          </button>
        </div>

        <nav className="sidebar__nav">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={closeSidebar}
            >
              <item.icon size={15} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <button
          className="sidebar__profile"
          type="button"
          onClick={() => {
            localStorage.removeItem('ikimina-session');
            showToast('You have been logged out.', 'info');
            navigate('/login');
          }}
        >
          <div className="avatar">DG</div>
          <div>
            <strong>Director General</strong>
            <small>Super Admin</small>
          </div>
          <LogOut size={15} />
        </button>
      </aside>

      <div className="shell-main">
        <header className="topbar">
          <button className="icon-button mobile-only" onClick={toggleSidebar} aria-label="Open menu">
            <Menu size={18} />
          </button>
          <div className="topbar__search">
            <Search size={15} />
            <input placeholder={`Search ${location.pathname === '/' ? 'dashboard' : location.pathname.slice(1)}...`} />
          </div>
          <div className="topbar__actions">
            <span className="date-pill">{date}</span>
            <button className="icon-button" aria-label="Notifications">
              <Bell size={16} />
            </button>
            <button className="user-chip">
              <span>
                <UserRound size={14} />
              </span>
              <strong>Admin</strong>
              <ChevronDown size={14} />
            </button>
          </div>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
