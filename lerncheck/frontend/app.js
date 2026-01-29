/* Lerncheck Frontend-Demo (localStorage)
   - Lehrer: Codes generieren, Test JSON uploaden, Statistik sehen
   - Schüler: per Code einloggen, Test machen, Ergebnis sehen, Feedback senden
   - Adaptive: Fragen mit hoher Fehlerquote werden in den nächsten Test aufgenommen
*/

const LS_KEY = "lerncheck_data_v1";

const $ = (id) => document.getElementById(id);

function loadData() {
  const raw = localStorage.getItem(LS_KEY);
  if (!raw) return makeFreshData();
  try {
    const data = JSON.parse(raw);
    return { ...makeFreshData(), ...data };
  } catch {
    return makeFreshData();
  }
}

function saveData(data) {
  localStorage.setItem(LS_KEY, JSON.stringify(data));
}

function makeFreshData() {
  return {
    students: [],               // { code, createdAt }
    activeTest: null,           // { id, title, questions:[...] }
    stagedTest: null,           // uploaded but not active yet
    submissions: [],            // { testId, studentCode, answers:{qid:optIndex}, score, createdAt }
    feedback: [],               // { testId, studentCode, text, createdAt }
    hardQuestionPool: {}        // testId -> [question objects] (aggregated "hard" questions for next test)
  };
}

function uid() {
  return Math.random().toString(16).slice(2) + Date.now().toString(16);
}

function genCode() {
  const chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
  let s = "";
  for (let i = 0; i < 6; i++) s += chars[Math.floor(Math.random() * chars.length)];
  return s;
}

function escapeHtml(s) {
  return String(s).replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
}

function showScreen(name) {
  const screens = ["screenHome","screenTeacher","screenStudentLogin","screenStudentTest","screenStudentResult"];
  screens.forEach(s => $(s).classList.add("hidden"));
  $(name).classList.remove("hidden");
}

/* ---------- Global state ---------- */
let data = loadData();
let currentStudentCode = null;
let currentTestRun = null; // { testId, questions, answers }

/* ---------- Home navigation ---------- */
$("goTeacher").addEventListener("click", () => {
  renderTeacher();
  showScreen("screenTeacher");
});

$("goStudent").addEventListener("click", () => {
  renderStudentLogin();
  showScreen("screenStudentLogin");
});

$("backFromTeacher").addEventListener("click", () => showScreen("screenHome"));
$("backFromStudentLogin").addEventListener("click", () => showScreen("screenHome"));

$("btnReset").addEventListener("click", () => {
  if (!confirm("Wirklich alle lokalen Daten löschen?")) return;
  localStorage.removeItem(LS_KEY);
  data = loadData();
  currentStudentCode = null;
  currentTestRun = null;
  renderTeacher();
  renderStudentLogin();
  alert("Reset abgeschlossen.");
});

/* ---------- Teacher: students ---------- */
$("btnCreateStudents").addEventListener("click", () => {
  const n = Math.max(1, Math.min(40, Number($("studentCount").value || 1)));
  // replace existing codes (simple)
  data.students = [];
  const used = new Set();
  for (let i = 0; i < n; i++) {
    let c = genCode();
    while (used.has(c)) c = genCode();
    used.add(c);
    data.students.push({ code: c, createdAt: new Date().toISOString() });
  }
  saveData(data);
  renderTeacher();
});

/* ---------- Teacher: test upload ---------- */
$("btnUploadTest").addEventListener("click", async () => {
  const file = $("testFile").files?.[0];
  if (!file) {
    $("testInfo").textContent = "Bitte eine JSON-Datei auswählen.";
    return;
  }
  try {
    const text = await file.text();
    const test = JSON.parse(text);

    // Basic validation
    if (!test || typeof test !== "object") throw new Error("Ungültiges JSON");
    if (!Array.isArray(test.questions) || test.questions.length === 0) throw new Error("questions[] fehlt oder leer");

    // Normalize test
    const staged = {
      id: uid(),
      title: test.title || "Test",
      questions: test.questions.map((q, idx) => normalizeQuestion(q, idx))
    };

    // Optionally include hard questions from previous active test
    const includeHard = $("includeHardQuestions").checked;
    if (includeHard) {
      const hard = getHardQuestionsForNextTest();
      if (hard.length) {
        staged.questions = mergeUniqueQuestions(staged.questions, hard);
      }
    }

    data.stagedTest = staged;
    saveData(data);
    $("btnActivateTest").disabled = false;
    $("testInfo").textContent = `Test geladen: "${staged.title}" • Fragen: ${staged.questions.length}. Jetzt "Test aktivieren" klicken.`;
  } catch (e) {
    $("testInfo").textContent = `Fehler beim Laden: ${e?.message || e}`;
  }
});

$("btnActivateTest").addEventListener("click", () => {
  if (!data.stagedTest) return;
  data.activeTest = data.stagedTest;
  data.stagedTest = null;

  // Optional: clear submissions/feedback for previous test? keep them for history
  saveData(data);
  $("btnActivateTest").disabled = true;
  renderTeacher();
  alert("Test ist jetzt aktiv.");
});

function normalizeQuestion(q, idx) {
  // Supports:
  // { id?, topic, text, options:[...], correctIndex }
  const id = q.id || `q_${idx+1}_${uid().slice(0,6)}`;
  const topic = q.topic || "Allgemein";
  const text = q.text || q.question || `Frage ${idx+1}`;
  const options = Array.isArray(q.options) ? q.options : [];
  const correctIndex = Number.isInteger(q.correctIndex) ? q.correctIndex : 0;
  if (!options.length) throw new Error(`Frage "${text}" hat keine options[]`);
  if (correctIndex < 0 || correctIndex >= options.length) throw new Error(`Frage "${text}": correctIndex ungültig`);
  return { id, topic, text, options, correctIndex };
}

function mergeUniqueQuestions(base, extra) {
  const seen = new Set(base.map(q => q.id));
  const out = [...base];
  for (const q of extra) {
    if (!seen.has(q.id)) out.push(q);
  }
  return out;
}

/* ---------- Teacher: stats & feedback ---------- */
$("btnRefreshStats").addEventListener("click", () => {
  renderTeacherStats();
});

$("btnRefreshFeedback").addEventListener("click", () => {
  renderTeacherFeedback();
});

$("btnExportData").addEventListener("click", () => {
  $("debugOut").textContent = JSON.stringify(data, null, 2);
});

/* ---------- Student login ---------- */
$("btnStudentLogin").addEventListener("click", () => {
  const code = $("studentCodeInput").value.trim().toUpperCase();
  const ok = data.students.some(s => s.code === code);
  if (!ok) {
    $("studentLoginMsg").textContent = "❌ Ungültiger Code. Bitte den Code vom Lehrer nutzen.";
    $("studentLoginMsg").className = "small";
    return;
  }
  if (!data.activeTest) {
    $("studentLoginMsg").textContent = "⚠️ Kein aktiver Test. Bitte später erneut versuchen.";
    $("studentLoginMsg").className = "small muted";
    return;
  }
  currentStudentCode = code;
  startStudentTest();
});

$("exitStudentTest").addEventListener("click", () => {
  if (!confirm("Test wirklich abbrechen?")) return;
  currentTestRun = null;
  showScreen("screenStudentLogin");
});

$("btnSubmitTest").addEventListener("click", () => {
  submitStudentTest();
});

$("backToStudentLogin").addEventListener("click", () => {
  // reset state
  $("feedbackText").value = "";
  $("feedbackMsg").textContent = "";
  currentTestRun = null;
  showScreen("screenStudentLogin");
  renderStudentLogin();
});

$("btnSendFeedback").addEventListener("click", () => {
  const text = $("feedbackText").value.trim();
  if (!text) {
    $("feedbackMsg").textContent = "Bitte Text eingeben.";
    return;
  }
  if (!data.activeTest) {
    $("feedbackMsg").textContent = "Kein aktiver Test.";
    return;
  }
  const testId = data.activeTest.id;
  data.feedback.push({
    testId,
    studentCode: currentStudentCode || "UNKNOWN",
    text,
    createdAt: new Date().toISOString()
  });
  saveData(data);
  $("feedbackMsg").textContent = "✅ Feedback gesendet (anonym).";
  $("feedbackText").value = "";
});

/* ---------- Render functions ---------- */
function renderTeacher() {
  // Codes
  const box = $("studentCodes");
  box.innerHTML = "";
  if (!data.students.length) {
    box.innerHTML = `<div class="muted small">Noch keine Codes. Bitte generieren.</div>`;
  } else {
    data.students.forEach(s => {
      const el = document.createElement("div");
      el.className = "code-pill";
      el.textContent = s.code;
      box.appendChild(el);
    });
  }

  // Active test badge + info
  if (data.activeTest) {
    $("activeTestBadge").textContent = `Aktiv: ${data.activeTest.title}`;
  } else {
    $("activeTestBadge").textContent = "Kein aktiver Test";
  }

  // staged test info
  if (data.stagedTest) {
    $("testInfo").textContent = `Bereit zum Aktivieren: "${data.stagedTest.title}" (${data.stagedTest.questions.length} Fragen)`;
    $("btnActivateTest").disabled = false;
  } else {
    $("btnActivateTest").disabled = true;
  }

  renderTeacherStats();
  renderTeacherFeedback();
}

function renderTeacherStats() {
  const stats = computeClassStats();
  const box = $("statsBox");
  if (!data.activeTest) {
    box.innerHTML = `<div class="muted small">Kein aktiver Test. Bitte Test aktivieren.</div>`;
    return;
  }

  const kpi = `
    <div class="kpi">
      <div class="tile">
        <div class="num">${stats.totalSubmissions}</div>
        <div class="label">Abgaben (anonym)</div>
      </div>
      <div class="tile">
        <div class="num">${stats.avgScore}%</div>
        <div class="label">Ø Score (Klasse)</div>
      </div>
      <div class="tile">
        <div class="num">${stats.hardCount}</div>
        <div class="label">Schwere Fragen (für nächsten Test)</div>
      </div>
    </div>
  `;

  const list = stats.perQuestion
    .sort((a,b) => b.wrongRate - a.wrongRate)
    .map(q => {
      const pct = Math.round(q.wrongRate * 100);
      const w = Math.min(100, Math.max(0, pct));
      const color = w >= 60 ? "var(--danger)" : w >= 35 ? "var(--accent2)" : "var(--ok)";
      return `
        <div class="heat-item">
          <div class="row space-between">
            <div><strong>${escapeHtml(q.topic)}</strong> — ${escapeHtml(q.text)}</div>
            <div class="badge">${pct}% falsch</div>
          </div>
          <div class="bar"><div style="width:${w}%; background:${color}"></div></div>
        </div>
      `;
    }).join("");

  box.innerHTML = `
    ${kpi}
    <div class="hr"></div>
    <div class="muted small">Themenhitze (Fehlerquote je Frage):</div>
    <div class="heat">${list || `<div class="muted small">Noch keine Abgaben.</div>`}</div>
  `;
}

function renderTeacherFeedback() {
  const box = $("feedbackBox");
  if (!data.activeTest) {
    box.innerHTML = `<div class="muted small">Kein aktiver Test.</div>`;
    return;
  }
  const testId = data.activeTest.id;
  const items = data.feedback
    .filter(f => f.testId === testId)
    .sort((a,b) => b.createdAt.localeCompare(a.createdAt));

  if (!items.length) {
    box.innerHTML = `<div class="muted small">Noch kein Feedback eingegangen.</div>`;
    return;
  }
  box.innerHTML = items.map(f => `
    <div class="msg">
      <div class="muted small">${new Date(f.createdAt).toLocaleString()}</div>
      <div>${escapeHtml(f.text)}</div>
    </div>
  `).join("");
}

function renderStudentLogin() {
  $("studentCodeInput").value = "";
  $("studentLoginMsg").textContent = "";
  if (data.activeTest) {
    $("studentActiveTestInfo").textContent = `Aktiver Test: "${data.activeTest.title}" • Fragen: ${data.activeTest.questions.length}`;
  } else {
    $("studentActiveTestInfo").textContent = "Derzeit ist kein Test aktiv.";
  }
}

/* ---------- Student test flow ---------- */
function startStudentTest() {
  if (!data.activeTest) return;

  // Prevent re-submission? allow re-run but only last counts? We'll allow multiple attempts but stats are per submission.
  const t = data.activeTest;
  currentTestRun = {
    testId: t.id,
    title: t.title,
    questions: t.questions,
    answers: {} // qid -> optIndex
  };

  $("studentTestTitle").textContent = `Test: ${t.title}`;
  renderQuestions();
  showScreen("screenStudentTest");
}

function renderQuestions() {
  const area = $("questionArea");
  area.innerHTML = "";

  const qs = currentTestRun.questions;
  qs.forEach((q, idx) => {
    const wrap = document.createElement("div");
    wrap.className = "q";
    wrap.innerHTML = `
      <div class="q-title">${idx+1}. ${escapeHtml(q.text)} <span class="muted small">(${escapeHtml(q.topic)})</span></div>
      <div class="options" id="opts_${q.id}"></div>
    `;
    area.appendChild(wrap);

    const opts = wrap.querySelector(`#opts_${CSS.escape(q.id)}`);
    q.options.forEach((opt, oi) => {
      const label = document.createElement("label");
      label.className = "opt";
      label.innerHTML = `
        <input type="radio" name="q_${q.id}" value="${oi}">
        <div>${escapeHtml(opt)}</div>
      `;
      label.querySelector("input").addEventListener("change", (e) => {
        currentTestRun.answers[q.id] = Number(e.target.value);
        updateProgress();
      });
      opts.appendChild(label);
    });
  });

  updateProgress();
}

function updateProgress() {
  const total = currentTestRun.questions.length;
  const answered = Object.keys(currentTestRun.answers).length;
  $("progressInfo").textContent = `Beantwortet: ${answered}/${total}`;
}

function submitStudentTest() {
  const qs = currentTestRun.questions;
  const total = qs.length;
  const answered = Object.keys(currentTestRun.answers).length;
  if (answered < total) {
    if (!confirm(`Du hast ${total-answered} Frage(n) nicht beantwortet. Trotzdem abgeben?`)) return;
  }

  // Compute score
  let correct = 0;
  const perQ = [];
  for (const q of qs) {
    const a = currentTestRun.answers[q.id];
    const ok = (a === q.correctIndex);
    if (ok) correct++;
    perQ.push({ q, a, ok });
  }
  const scorePct = Math.round((correct / total) * 100);

  data.submissions.push({
    testId: currentTestRun.testId,
    studentCode: currentStudentCode,
    answers: currentTestRun.answers,
    score: scorePct,
    createdAt: new Date().toISOString()
  });

  // Update "hard questions" pool for next test based on class stats
  updateHardQuestionPoolForTest(currentTestRun.testId);

  saveData(data);

  // Render student result
  const box = $("studentResultBox");
  box.innerHTML = `
    <div class="kpi">
      <div class="tile">
        <div class="num">${scorePct}%</div>
        <div class="label">Dein Score</div>
      </div>
      <div class="tile">
        <div class="num">${correct}/${total}</div>
        <div class="label">Richtig</div>
      </div>
    </div>
    <div class="hr"></div>
    <div><strong>Auswertung:</strong></div>
    ${perQ.map(({q,a,ok}) => {
      const chosen = (a === undefined) ? "—" : q.options[a];
      const correctOpt = q.options[q.correctIndex];
      const tag = ok ? `<span class="badge" style="color:var(--ok)">Richtig</span>` : `<span class="badge" style="color:var(--danger)">Falsch</span>`;
      return `
        <div class="rowline">
          <div>
            <div><strong>${escapeHtml(q.text)}</strong> <span class="muted small">(${escapeHtml(q.topic)})</span></div>
            <div class="muted small">Deine Antwort: ${escapeHtml(chosen)} • Richtig: ${escapeHtml(correctOpt)}</div>
          </div>
          <div>${tag}</div>
        </div>
      `;
    }).join("")}
  `;

  showScreen("screenStudentResult");
}

/* ---------- Stats & adaptive ---------- */
function computeClassStats() {
  if (!data.activeTest) {
    return { totalSubmissions: 0, avgScore: 0, perQuestion: [], hardCount: 0 };
  }
  const t = data.activeTest;
  const subs = data.submissions.filter(s => s.testId === t.id);
  const totalSub = subs.length;
  const avgScore = totalSub ? Math.round(subs.reduce((a,s)=>a+s.score,0)/totalSub) : 0;

  // per question wrong rate
  const perQuestion = t.questions.map(q => {
    let totalAns = 0;
    let wrong = 0;
    for (const s of subs) {
      const a = s.answers?.[q.id];
      if (a === undefined) continue;
      totalAns++;
      if (a !== q.correctIndex) wrong++;
    }
    const wrongRate = totalAns ? (wrong / totalAns) : 0;
    return { qid: q.id, topic: q.topic, text: q.text, wrongRate, totalAns };
  });

  const hard = getHardQuestionsForNextTest(); // from pool of last active test
  return { totalSubmissions: totalSub, avgScore, perQuestion, hardCount: hard.length };
}

// Define “hard”: wrongRate >= 0.5 and at least 3 answers (configurable)
function updateHardQuestionPoolForTest(testId) {
  const active = data.activeTest;
  if (!active || active.id !== testId) return;

  const subs = data.submissions.filter(s => s.testId === testId);
  const pool = [];

  for (const q of active.questions) {
    let totalAns = 0;
    let wrong = 0;
    for (const s of subs) {
      const a = s.answers?.[q.id];
      if (a === undefined) continue;
      totalAns++;
      if (a !== q.correctIndex) wrong++;
    }
    const wrongRate = totalAns ? wrong / totalAns : 0;
    if (totalAns >= 3 && wrongRate >= 0.5) {
      pool.push(q);
    }
  }

  data.hardQuestionPool[testId] = pool;
}

function getHardQuestionsForNextTest() {
  // take hard questions from current active test (to be added into the next one)
  if (!data.activeTest) return [];
  const testId = data.activeTest.id;
  return data.hardQuestionPool[testId] || [];
}

/* ---------- Init ---------- */
function init() {
  // initial render for home
  $("testInfo").textContent = "";
  renderTeacher();
  renderStudentLogin();
  showScreen("screenHome");
}

init();