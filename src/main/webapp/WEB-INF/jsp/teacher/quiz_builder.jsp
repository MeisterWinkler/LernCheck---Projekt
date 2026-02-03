<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Quiz erstellen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
  <script>
    let qIndex = 1;

    function addQuestion() {
      const container = document.getElementById("questions");
      const idx = qIndex++;

      const block = document.createElement("div");
      block.className = "card";
      block.style.marginTop = "12px";
      block.innerHTML = `
        <h3>Frage ${idx+1}</h3>
        <label>Fragetext</label>
        <input name="q_text_${idx}" required>

        <label>Antwort A</label>
        <input name="q_${idx}_a" required>
        <label>Antwort B</label>
        <input name="q_${idx}_b" required>
        <label>Antwort C</label>
        <input name="q_${idx}_c" required>
        <label>Antwort D</label>
        <input name="q_${idx}_d" required>

        <label>Richtige Antwort</label>
        <select name="q_correct_${idx}" required>
          <option value="A">A</option>
          <option value="B">B</option>
          <option value="C">C</option>
          <option value="D">D</option>
        </select>
      `;
      container.appendChild(block);

      document.getElementById("questionCount").value = (idx + 1).toString();
    }
  </script>
</head>

<body class="container">

<div class="topbar">
  <h1>Quiz erstellen</h1>
  <div class="topbar-right">
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Dashboard</a>
  </div>
</div>

<form method="post" action="${pageContext.request.contextPath}/teacher/quiz/builder">
  <div class="card">
    <label>Klasse auswählen</label>
    <select name="classId" required>
      <c:forEach items="${classes}" var="k">
        <option value="${k.id}">${k.name}</option>
      </c:forEach>
    </select>

    <label style="margin-top:12px;">Thema / Titel</label>
    <input name="title" placeholder="z.B. Subnetting" required>

    <input type="hidden" id="questionCount" name="questionCount" value="1">
  </div>

  <div id="questions">
    <div class="card" style="margin-top:12px;">
      <h3>Frage 1</h3>
      <label>Fragetext</label>
      <input name="q_text_0" required>

      <label>Antwort A</label>
      <input name="q_0_a" required>
      <label>Antwort B</label>
      <input name="q_0_b" required>
      <label>Antwort C</label>
      <input name="q_0_c" required>
      <label>Antwort D</label>
      <input name="q_0_d" required>

      <label>Richtige Antwort</label>
      <select name="q_correct_0" required>
        <option value="A">A</option>
        <option value="B">B</option>
        <option value="C">C</option>
        <option value="D">D</option>
      </select>
    </div>
  </div>

  <div style="margin-top:12px; display:flex; gap:12px;">
    <button type="button" class="btn secondary" onclick="addQuestion()">Weitere Frage hinzufügen</button>
    <button type="submit" class="btn">Quiz erstellen</button>
  </div>

  <c:if test="${not empty error}">
    <div class="card" style="margin-top:12px;">
      <p class="error">${error}</p>
    </div>
  </c:if>
</form>

</body>
</html>