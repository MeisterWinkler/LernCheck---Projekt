export function getToken() { return localStorage.getItem("teacherToken"); }
export function setToken(t) { localStorage.setItem("teacherToken", t); }
export function clearToken() { localStorage.removeItem("teacherToken"); }

export function qs(name) {
  const u = new URL(window.location.href);
  return u.searchParams.get(name);
}

export function nav(url) { window.location.href = url; }

export async function api(path, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  if (!headers["Content-Type"] && options.body) headers["Content-Type"] = "application/json";

  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(path, { ...options, headers });
  const ct = res.headers.get("content-type") || "";

  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(`${res.status} ${res.statusText} ${txt}`.trim());
  }
  return ct.includes("application/json") ? res.json() : res.text();
}

export function requireTeacherAuth() {
  const t = getToken();
  if (!t) {
    window.location.href = "/teacher/login.html";
    return false;
  }
  return true;
}