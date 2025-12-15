//import { auth } from '../lib/firebase';

// const API_BASE = "http://localhost:8080";

// export async function api(path, init = {}) {
//   const response = await fetch(`${API_BASE}${path}`, init);

//   if (!response.ok) {
//     throw new Error(`${response.status} ${response.statusText}`);
//   }

//   return response.json();
// }

export async function api(url, options = {}) {
  const res = await fetch(`http://localhost:8080${url}`, {
    credentials: "include",
    ...options,
  });

  if (!res.ok) {
    throw new Error("API error");
  }

  return res.json();
}
