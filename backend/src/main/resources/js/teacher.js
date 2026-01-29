// Summary: Lehrer-Frontend: Auth, Klassen/Quiz erstellen, Fragen sammeln+upload, Heatmap anzeigen.
import { $, getToken, setToken, clearToken, getJson, postJson, showMsg } from "./config.js";

const isLoginPage = window.location.pathname.endsWith("teacher-login.html") || window.location.pathname.endsWith("teacher-login");
const isDashboard = window.location.pathname.endsWith("teacher-dashboard.html") || window.location.pathname.endsWith("teacher-dashboard");

function requireAuth() {
  const token = getToken();
  if (!token) {
    window.location.href = "teacher-login.html";
    return false;
  }
  return true;
}

// ---------- Login Page ----------
async function handleLoginPage() {
  const loginMsg = $("loginMsg");
  const regMsg = $("regMsg");

  $("btnLogin").onclick = async () => {
    try {
      showMsg(loginMsg, "Login...", "info");
      const email = $("loginEmail").value.trim();
      const password = $("loginPw").value;
      const data = await postJson("/api/auth/login", { email, password }, false);
      setToken(data.token);
      showMsg(loginMsg, "Login erfolgreich.", "ok");
      window.location.href = "teacher-dashboard.html";
    } catch (e) {
      showMsg(loginMsg, "Login fehlgeschlagen.", "error");
    }
  };

  $("btnRegister").onclick = async () => {
    try {
      showMsg(regMsg, "Registrierung...", "info");
      const email = $("regEmail").value.trim();
      const displayName = $("regName").value.trim();
      const password = $("regPw").value;
      const data = await postJson("/api/auth/register", { email, displayName, password }, false);
      setToken(data.token);
      showMsg(regMsg, "Registrierung erfolgreich.", "ok");
      window.location.href = "teacher-dashboard.html";
    } catch (e) {
      showMsg(regMsg, "Registrierung fehlgeschlagen.", "error");
    }
  };
}

// ---------- Dashboard ----------
let pendingQuestions = [];

function renderPendingQuestions() {
  const wrap = $("questionList");
  const count = $("qCount");
  count.textContent = `${pendingQuestions.length} in Liste`;

  if (pendingQuestions.length === 0) {
    wrap.innerHTML = `<p class="badge">Keine Fragen in der Liste.</p>`;
    return;
  }

  wrap.innerHTML = pendingQuestions.map((q, i) => `
    <div class="card" style="margin:10px 0;">
      <div class="row">
        <strong>#${i + 1}</strong>
        <span class="badge">Correct: ${q.correctOption}</span>
      </div>
      <p>${escapeHtml(q.text)}</p>
      <p class="badge">A: ${escapeHtml(q.optionA)}</p>
      <p class="badge">B: ${escapeHtml(q.optionB)}</p>
      <p class="badge">C: ${escapeHtml(q.optionC)}</p>
      <p class="badge">D: ${escapeHtml(q.optionD)}</p>
    </div>
  `).join("");
}

function escapeHtml(s) {
  return (s ?? "").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
}

async function loadClassesInto(selectId) {
  const sel = $(selectId);
  sel.innerHTML = "";
  const classes = await getJson("/api/teacher/classes", true);

  if (classes.length === 0) {
    sel.innerHTML = `<option value="">(Keine Klassen)</option>`;
    return classes;
  }

  for (const c of classes) {
    const opt = document.createElement("option");
    opt.value = c.id;
    opt.textContent = c.name;
    sel.appendChild(opt);
  }
  return classes;
}

async function loadQuizzesForClass(classId, selectId) {
  const sel = $(selectId);
  sel.innerHTML = "";

  if (!classId) {
    sel.innerHTML = `<option value="">(Erst Klasse wählen)</option>`;
    return [];
  }

  const quizzes = await getJson(`/api/teacher/classes/${classId}/quizzes`, true);

  if (quizzes.length === 0) {
    sel.innerHTML = `<option value="">(Keine Quizze)</option>`;
    return quizzes;
  }

  for (const q of quizzes) {
    const opt = document.createElement("option");
    opt.value = q.id;
    opt.textContent = `${q.title} (PIN: ${q.joinPin})`;
    sel.appendChild(opt);
  }
  return quizzes;
}

async function handleDashboard() {
  if (!requireAuth()) return;

  const authState = $("authState");
  authState.textContent = "eingeloggt";

  $("btnLogout").onclick = () => {
    clearToken();
    window.location.href = "teacher-login.html";
  };

  // Initial load
  try {
    await loadClassesInto("classSelect");
    const classId = $("classSelect").value;
    await loadQuizzesForClass(classId, "quizSelect");
    await loadQuizzesForClass(classId, "heatmapQuizSelect");
  } catch (e) {
    // ignore for now
  }

  $("classSelect").onchange = async () => {
    const classId = $("classSelect").value;
    await loadQuizzesForClass(classId, "quizSelect");
    await loadQuizzesForClass(classId, "heatmapQuizSelect");
  };

  $("btnCreateClass").onclick = async () => {
    const msgEl = $("classMsg");
    try {
      showMsg(msgEl, "Erstelle Klasse...", "info");
      const name = $("className").value.trim();
      if (!name) throw new Error("name");
      await postJson("/api/teacher/classes", { name }, true);
      showMsg(msgEl, "Klasse erstellt.", "ok");
      $("className").value = "";
      await loadClassesInto("classSelect");
    } catch (e) {
      showMsg(msgEl, "Fehler beim Erstellen der Klasse.", "error");
    }
  };

  $("btnCreateQuiz").onclick = async () => {
    const msgEl = $("quizMsg");
    try {
      showMsg(msgEl, "Erstelle Quiz...", "info");
      const classId = $("classSelect").value;
      const title = $("quizTitle").value.trim();
      if (!classId || !title) throw new Error("missing");
      const quiz = await postJson(`/api/teacher/classes/${classId}/quizzes`, { title }, true);
      showMsg(msgEl, `Quiz erstellt. PIN: ${quiz.joinPin}`, "ok");
      $("quizTitle").value = "";
      await loadQuizzesForClass(classId, "quizSelect");
      await loadQuizzesForClass(classId, "heatmapQuizSelect");
    } catch (e) {
      showMsg(msgEl, "Fehler beim Erstellen des Quiz.", "error");
    }
  };

  $("btnLoadQuizzes").onclick = async () => {
    const classId = $("classSelect").value;
    await loadQuizzesForClass(classId, "quizSelect");
    await loadQuizzesForClass(classId, "heatmapQuizSelect");
  };

  $("btnAddQuestion").onclick = () => {
    const q = {
      text: $("qText").value.trim(),
      optionA: $("qA").value.trim(),
      optionB: $("qB").value.trim(),
      optionC: $("qC").value.trim(),
      optionD: $("qD").value.trim(),
      correctOption: $("qCorrect").value.trim().toUpperCase(),
    };

    if (!q.text || !q.optionA || !q.optionB || !q.optionC || !q.optionD) return;
    if (!["A","B","C","D"].includes(q.correctOption)) return;

    pendingQuestions.push(q);
    renderPendingQuestions();

    $("qText").value = "";
    $("qA").value = "";
    $("qB").value = "";
    $("qC").value = "";
    $("qD").value = "";
    $("qCorrect").value = "A";
  };

  $("btnClearQuestions").onclick = () => {
    pendingQuestions = [];
    renderPendingQuestions();
  };

  $("btnUploadQuestions").onclick = async () => {
    const msgEl = $("questionMsg");
    try {
      showMsg(msgEl, "Upload läuft...", "info");
      const quizId = $("quizSelect").value;
      if (!quizId) throw new Error("quiz");
      if (pendingQuestions.length === 0) throw new Error("none");

      await postJson(`/api/teacher/quizzes/${quizId}/questions`, pendingQuestions, true);
      showMsg(msgEl, "Fragen hochgeladen.", "ok");
      pendingQuestions = [];
      renderPendingQuestions();
    } catch (e) {
      showMsg(msgEl, "Upload fehlgeschlagen (Quiz wählen + Fragenliste füllen).", "error");
    }
  };

  $("btnLoadHeatmap").onclick = async () => {
    const msgEl = $("heatmapMsg");
    const wrap = $("heatmapTableWrap");
    try {
      showMsg(msgEl, "Heatmap wird geladen...", "info");
      wrap.innerHTML = "";
      const quizId = $("heatmapQuizSelect").value;
      if (!quizId) throw new Error("quiz");

      const data = await getJson(`/api/teacher/stats/heatmap/${quizId}`, true);

      const points = Object.values(data.byQuestionId || {});
      points.sort((a,b) => b.wrongRate - a.wrongRate);

      wrap.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>Question ID</th>
              <th>Antworten</th>
              <th>Fehlerquote</th>
            </tr>
          </thead>
          <tbody>
            ${points.map(p => `
              <tr>
                <td>${p.questionId}</td>
                <td>${p.totalAnswers}</td>
                <td>${Math.round(p.wrongRate * 100)}%</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      `;

      showMsg(msgEl, "Heatmap geladen.", "ok");
    } catch (e) {
      showMsg(msgEl, "Heatmap konnte nicht geladen werden.", "error");
    }
  };

  renderPendingQuestions();
}

if (isLoginPage) handleLoginPage();
if (isDashboard) handleDashboard();