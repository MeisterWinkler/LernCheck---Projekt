// Summary: Zentrale API-URL + kleine Helper-Funktionen.
export const API_BASE = "http://localhost:8080";

export function getToken() {
  return localStorage.getItem("jwt");
}

export function setToken(token) {
  localStorage.setItem("jwt", token);
}

export function clearToken() {
  localStorage.removeItem("jwt");
}

export async function getJson(path, auth = false) {
  const headers = {};
  if (auth) headers["Authorization"] = "Bearer " + getToken();

  const res = await fetch(API_BASE + path, { headers });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function postJson(path, body, auth = false) {
  const headers = { "Content-Type": "application/json" };
  if (auth) headers["Authorization"] = "Bearer " + getToken();

  const res = await fetch(API_BASE + path, {
    method: "POST",
    headers,
    body: JSON.stringify(body),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export function $(id) {
  return document.getElementById(id);
}

export function showMsg(el, text, type = "info") {
  el.textContent = text;
  el.dataset.type = type;
}