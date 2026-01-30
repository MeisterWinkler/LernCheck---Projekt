// Summary: Helper (Fetch mit JWT, Navigation, LocalStorage).
export function getToken() { return localStorage.getItem("teacherToken"); }
export function setToken(t) { localStorage.setItem("teacherToken", t); }
export function clearToken() { localStorage.removeItem("teacherToken"); }

export async function api(path, options = {}) {
  const headers = options.headers ? {...options.headers} : {};
  headers["Content-Type"] = "application/json";

  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(path, { ...options, headers });
  if (!res.ok) {
    const txt = await res.text().catch(()=> "");
    throw new Error(`${res.status} ${res.statusText} - ${txt}`);
  }
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
}

export function qs(name) {
  const u = new URL(window.location.href);
  return u.searchParams.get(name);
}

export function nav(url) { window.location.href = url; }