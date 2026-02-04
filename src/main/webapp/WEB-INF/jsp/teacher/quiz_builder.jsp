<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Quiz erstellen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">

  <script>
    function renumber() {
      const blocks = document.querySelectorAll(".qblock");
      blocks.forEach((b, idx) => {
        const h = b.querySelector(".qtitle");
        if (h) h.textContent = "Frage " + (idx + 1);
      });
    }

    function addQuestion() {
      const container = document.getElementById("questions");

      const block = document.createElement("div");
      block.className = "card qblock";
      block.style.marginTop = "12px";

      // WICHTIG: name=... ist identisch für alle Fragen => Browser sendet Arrays
      block.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; gap:12px;">
          <h3 class="qtitle" style="margin:0;">Frage</h3>
          <button type="button" class="btn secondary" onclick="removeQuestion(this)">Entfernen</button>
        </div>

        <label>Fragetext</label>
        <input name="qText" required>

        <label>Antwort A</label>
        <input name="aText" required>
        <label>Antwort B</label>
        <input name="bText" required>
        <label>Antwort C</label>
        <input name="cText" required>
        <label>Antwort D</label>
        <input name="dText" required>

        <label>Richtige Antwort</label>
        <select name="correct" required>
          <option value="A">A</option>
          <option value="B">B</option>
          <option value="C">C</option>
          <option value="D">D</option>
        </select>
      `;

      container.appendChild(block);
      renumber();
    }

    function removeQuestion(btn) {
      const block = btn.closest(".qblock");
      if (block) block.remove();
      renumber();
    }

    window.addEventListener("load", () => {
      renumber();
    });
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

    <c:if test="${not empty error}">
      <p class="error" style="margin-top:10px;">${error}</p>
    </c:if>
  </div>

  <div id="questions">
    <!-- Erste Frage: identische names => Arrays -->
    <div class="card qblock" style="margin-top:12px;">
      <h3 class="qtitle" style="margin-top:0;">Frage 1</h3>

      <label>Fragetext</label>
      <input name="qText" required>

      <label>Antwort A</label>
      <input name="aText" required>
      <label>Antwort B</label>
      <input name="bText" required>
      <label>Antwort C</label>
      <input name="cText" required>
      <label>Antwort D</label>
      <input name="dText" required>

      <label>Richtige Antwort</label>
      <select name="correct" required>
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
</form>

</body>
</html>