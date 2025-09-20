import { auth } from '../lib/firebase';

export async function api(path, init = {}) {
  const headers = new Headers(init.headers || {});
  const currentUser = auth.currentUser;
  if (currentUser) {
    const token = await currentUser.getIdToken();
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(path, { ...init, headers });
  if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
  return response.json();
}
