// Summary: Schüler-Frontend: PIN join, Quiz laden, Antworten sammeln, Submit, Ergebnis + adaptive Wiederholung.
import { $, getJson, postJson, showMsg } from "./config.js";

const isJoin = window.location.pathname.endsWith("student-join.html") || window.location.pathname.endsWith("student-join");
const isQuiz = window.location.pathname.endsWith("student-quiz.html") || window.location.pathname.endsWith("student-quiz");

function saveSession(session) {
  localStorage.setItem("studentSession", JSON.stringify(session));
}

function loadSession() {
  try { return JSON.parse(localStorage.getItem("studentSession") || "null"); }
  catch { return null; }
}

function clearSession() {
  localStorage.removeItem("studentSession");
}

function renderQuestions(questions) {
  const wrap = $("questionsWrap");
  wrap.innerHTML = "";

  if (!questions || questions.length === 0) {
    wrap.innerHTML = `<p class="badge">Keine Fragen vorhanden.</p>`;
    return;
  }

  for (const q of questions) {
    const card = document.createElement("div");
    card.className = "card";
    card.style.margin = "10px 0";
    card.innerHTML = `
      <h3 style="margin-top:0;">${escapeHtml(q.text)}</h3>

      ${optionRadio(q.id, "A", q.optionA)}
      ${optionRadio(q.id, "B", q.optionB)}
      ${optionRadio(q.id, "C", q.optionC)}
      ${optionRadio(q.id, "D", q.optionD)}
    `;
    wrap.appendChild(card);
  }
}

function optionRadio(qid, opt, label) {
  return `
    <label class="row" style="margin:6px 0;">
      <input type="radio" name="q_${qid}" value="${opt}" />
      <span>${opt}: ${escapeHtml(label)}</span>
    </label>
  `;
}

function escapeHtml(s) {
  return (s ?? "").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
}

async function handleJoin() {
  const msgEl = $("joinMsg");

  $("btnJoin").onclick = async () => {
    try {
      showMsg(msgEl, "Trete bei...", "info");
      const pin = $("pin").value.trim();
      if (!pin) throw new Error("pin");

      const join = await postJson("/api/student/join", { pin }, false);
      saveSession({ anonToken: join.anonToken, quizId: join.quizId, quizTitle: join.quizTitle });

      showMsg(msgEl, "Beigetreten. Weiterleitung...", "ok");
      window.location.href = "student-quiz.html";
    } catch (e) {
      showMsg(msgEl, "PIN ungültig oder Quiz nicht aktiv.", "error");
    }
  };
}

async function handleQuiz() {
  const msgEl = $("quizMsg");
  const titleEl = $("quizTitle");
  const resultWrap = $("resultWrap");

  const session = loadSession();
  if (!session) {
    showMsg(msgEl, "Keine Sitzung gefunden. Bitte PIN eingeben.", "error");
    return;
  }

  titleEl.textContent = session.quizTitle || `Quiz ${session.quizId}`;

  try {
    showMsg(msgEl, "Quiz wird geladen...", "info");
    const quiz = await getJson(`/api/student/quiz/${session.quizId}`, false);
    renderQuestions(quiz.questions);
    showMsg(msgEl, "Quiz geladen.", "ok");
  } catch (e) {
    showMsg(msgEl, "Quiz konnte nicht geladen werden.", "error");
    return;
  }

  $("btnSubmit").onclick = async () => {
    try {
      showMsg(msgEl, "Sende Antworten...", "info");

      const answers = collectAnswers(session.quizId);
      if (answers.length === 0) throw new Error("no answers");

      const res = await postJson("/api/student/submit", { anonToken: session.anonToken, answers }, false);

      resultWrap.innerHTML = `
        <div class="kpi">
          <div class="card">
            <h3>Richtig</h3>
            <p><strong>${res.correct}</strong> / ${res.total}</p>
          </div>
          <div class="card">
            <h3>Falsch</h3>
            <p><strong>${res.total - res.correct}</strong></p>
          </div>
        </div>

        <hr>

        <h3>Fehlerübersicht</h3>
        ${res.wrongQuestionIds.length === 0
          ? `<p class="badge">Keine Fehler 🎉</p>`
          : `<p>Falsch beantwortete Fragen (IDs): <span class="badge">${res.wrongQuestionIds.join(", ")}</span></p>`
        }

        <p class="badge">Hinweis: Dieser Attempt ist abgeschlossen. Für neue Teilnahme PIN neu eingeben.</p>
      `;

      showMsg(msgEl, "Abgegeben.", "ok");
    } catch (e) {
      showMsg(msgEl, "Abgabe fehlgeschlagen (alle Fragen beantworten).", "error");
    }
  };

  $("btnLoadRepeat").onclick = async () => {
    try {
      showMsg(msgEl, "Lade Wiederholungsfragen...", "info");
      const repeat = await getJson(`/api/student/adaptive/${session.quizId}?n=5`, false);
      renderQuestions(repeat);
      showMsg(msgEl, "Wiederholungsfragen geladen.", "ok");
    } catch (e) {
      showMsg(msgEl, "Adaptive Fragen konnten nicht geladen werden.", "error");
    }
  };
}

function collectAnswers() {
  const session = loadSession();
  const wrap = $("questionsWrap");
  const cards = wrap.querySelectorAll('input[type="radio"]:checked');

  // Wir lesen alle checked radios, dann bauen wir answer objects zusammen.
  const map = new Map();
  for (const input of cards) {
    const [_, idStr] = input.name.split("_");
    const qid = Number(idStr);
    map.set(qid, input.value);
  }

  // Es kann sein, dass nicht alle Fragen gewählt wurden; das prüfen wir nicht hart,
  // aber Submit wird dann ggf. als "nicht vollständig" angesehen.
  return Array.from(map.entries()).map(([questionId, chosenOption]) => ({ questionId, chosenOption }));
}

if (isJoin) handleJoin();
if (isQuiz) handleQuiz();