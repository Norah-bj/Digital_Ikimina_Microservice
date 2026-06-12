import { ArrowRight, ChartNoAxesCombined, Eye, EyeOff, LockKeyhole, Mail, UserRound } from 'lucide-react';
import { FormEvent, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from '../components/ToastProvider';
import type { MemberRole } from '../types';

type AuthMode = 'login' | 'signup' | 'forgot' | 'reset';

interface AuthPageProps {
  mode: AuthMode;
}

const roleRedirects: Record<MemberRole, string> = {
  SUPER_ADMIN: '/',
  ACCOUNTANT_ADMIN: '/savings',
  LOAN_COMMITTEE: '/loans',
  SECRETARY: '/members',
  MEMBER: '/',
};

const adminCredentials = {
  email: 'admin@ikimina.rw',
  password: 'password',
  role: 'SUPER_ADMIN' as MemberRole,
};

export default function AuthPage({ mode }: AuthPageProps) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [showPassword, setShowPassword] = useState(false);
  const [role, setRole] = useState<MemberRole>('SUPER_ADMIN');
  const [remember, setRemember] = useState(true);
  const [email, setEmail] = useState(mode === 'login' ? adminCredentials.email : '');
  const [password, setPassword] = useState(mode === 'login' ? adminCredentials.password : '');

  const copy = useMemo(() => {
    if (mode === 'signup') return { eyebrow: 'Create access', title: 'Create account', action: 'Create account' };
    if (mode === 'forgot') return { eyebrow: 'Recovery', title: 'Forgot password', action: 'Send reset link' };
    if (mode === 'reset') return { eyebrow: 'Recovery', title: 'Reset password', action: 'Reset password' };
    return { eyebrow: 'Secure access', title: 'Sign in', action: 'Open workspace' };
  }, [mode]);

  function submit(event: FormEvent) {
    event.preventDefault();
    if (mode === 'forgot') {
      showToast('Reset instructions were sent. Use the reset screen to continue.', 'info');
      navigate('/reset-password');
      return;
    }
    if (mode === 'signup') {
      showToast('Account request submitted. Sign in with the preset admin credentials.', 'success');
      navigate('/login', { replace: true });
      return;
    }
    if (mode === 'reset') {
      showToast('Password reset saved. Sign in with the preset admin credentials.', 'success');
      navigate('/login', { replace: true });
      return;
    }
    if (email !== adminCredentials.email || password !== adminCredentials.password) {
      showToast('Invalid credentials. Use admin@ikimina.rw and password.', 'error');
      return;
    }
    const resolvedRole = adminCredentials.role;
    const session = { role: resolvedRole, remember, signedInAt: new Date().toISOString() };
    localStorage.setItem('ikimina-session', JSON.stringify(session));
    showToast('Logged in successfully.', 'success');
    navigate(roleRedirects[resolvedRole], { replace: true });
  }

  return (
    <main className="login-page">
      <section className="login-visual">
        <div className="brand-mark large">
          <ChartNoAxesCombined size={24} />
        </div>
        <h1>Digital Ikimina</h1>
        <p>Secure cooperative finance for savings, loans, guarantees, repayments, and transparent member ownership.</p>
        <div className="fintech-illustration" aria-hidden="true">
          <div className="growth-card"><span>Savings</span><strong>+24%</strong></div>
          <div className="network-orbit">
            <span /> <span /> <span />
          </div>
          <div className="ledger-card"><span>Loan book</span><strong>Healthy</strong></div>
        </div>
      </section>
      <section className="login-form-panel">
        <form className="login-form" onSubmit={submit}>
          <span className="eyebrow">{copy.eyebrow}</span>
          <h2>{copy.title}</h2>
          {mode === 'signup' && (
            <label>
              <span>Full name</span>
              <div><UserRound size={15} /><input required placeholder="Your name" /></div>
            </label>
          )}
          <label>
            <span>Email</span>
            <div><Mail size={15} /><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /></div>
          </label>
          {mode !== 'forgot' && (
            <label>
              <span>{mode === 'reset' ? 'New password' : 'Password'}</span>
              <div>
                <LockKeyhole size={15} />
                <input required type={showPassword ? 'text' : 'password'} value={password} onChange={(event) => setPassword(event.target.value)} />
                <button className="input-icon" type="button" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'}>
                  {showPassword ? <EyeOff size={14} /> : <Eye size={14} />}
                </button>
              </div>
            </label>
          )}
          {mode === 'signup' && (
            <label>
              <span>Role</span>
              <div>
                <UserRound size={15} />
                <select value={role} onChange={(event) => setRole(event.target.value as MemberRole)} aria-label="User role">
                  <option value="SUPER_ADMIN">Super Admin</option>
                  <option value="ACCOUNTANT_ADMIN">Accountant Admin</option>
                  <option value="LOAN_COMMITTEE">Loan Committee</option>
                  <option value="SECRETARY">Secretary</option>
                  <option value="MEMBER">Member</option>
                </select>
              </div>
            </label>
          )}
          {mode === 'login' && <p className="preset-credentials">Preset admin: admin@ikimina.rw / password</p>}
          {mode === 'login' && (
            <div className="auth-row">
              <label className="check-row">
                <input type="checkbox" checked={remember} onChange={(event) => setRemember(event.target.checked)} />
                <span>Remember me</span>
              </label>
              <Link to="/forgot-password">Forgot password?</Link>
            </div>
          )}
          <button className="button primary login-button" type="submit">
            {copy.action}
            <ArrowRight size={15} />
          </button>
          <div className="auth-switch">
            {mode === 'login' ? <span>Need access? <Link to="/signup">Create account</Link></span> : <span>Already have access? <Link to="/login">Sign in</Link></span>}
          </div>
        </form>
      </section>
    </main>
  );
}
