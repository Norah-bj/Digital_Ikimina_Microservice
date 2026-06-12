import { Navigate, Outlet, useLocation } from 'react-router-dom';

export function isAuthenticated() {
  return Boolean(localStorage.getItem('ikimina-session'));
}

export default function ProtectedRoute() {
  const location = useLocation();

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
