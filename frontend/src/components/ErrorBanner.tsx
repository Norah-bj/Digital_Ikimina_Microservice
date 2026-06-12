import { useEffect } from 'react';
import { useToast } from './ToastProvider';

export default function ErrorBanner({ message }: { message?: string }) {
  const { showToast } = useToast();

  useEffect(() => {
    if (message) showToast(message, 'error');
  }, [message, showToast]);

  return null;
}
