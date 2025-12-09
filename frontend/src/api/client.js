import { auth } from '../lib/firebase';

export async function api(path, init = {}) {
  const headers = new Headers(init.headers || {});
  const currentUser = auth.currentUser;
  if (currentUser) {
    const token = await currentUser.getIdToken();
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(path, { ...init, headers });

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`;

    try {
      const data = await response.clone().json();
      if (data) {
        if (typeof data.detail === 'string' && data.detail.trim()) {
          // Spring ProblemDetails `detail` (Boot 3+)
          message = data.detail.trim();
        } else if (typeof data.message === 'string' && data.message.trim()) {
          message = data.message.trim();
        } else if (typeof data.error === 'string' && data.error.trim()) {
          message = data.error.trim();
        }
      }
    } catch {
      try {
        const text = await response.text();
        if (text && text.trim()) {
          message = text.trim();
        }
      } catch {
        // ignore body parsing errors and keep default message
      }
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
