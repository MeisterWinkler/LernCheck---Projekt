<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Quiz neu erstellen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
  <script>
    let qCount = 1;
    function addQuestion() {
      qCount++;
      const wrap = document.getElementById("questions");
      const idx = qCount-1;

      const html = `
        <div class="card">
          <h3>Frage ${qCount}</h3>
          <label>Frage:</label>
          <textarea name="qText" required></textarea>

          <div class="two-col">
            <div>
              <label>Antwort A:</label>
              <input name="aText" required>
            </div>
            <div>
              <label>Antwort B:</label>
              <input name="bText" required>
            </div>
            <div>
              <label>Antwort C:</label>
              <input name="cText" required>
            </div>
            <div>
              <label>Antwort D:</label>
              <input name="dText" required>
            </div>
          </div>

          <label>Richtige Antwort:</label>
          <select name="correct">
            <option value="A">A</option>
            <option value="B">B</option>
            <option value="C">C</option>
            <option value="D">D</option>
          </select>
        </div>
      `;
      const div = document.createElement("div");
      div.innerHTML = html;
      wrap.appendChild(div);
    }
  </script>
</head>
<body class="container">
  <div class="topbar">
    <h1>Quiz neu erstellen</h1>
    <div class="topbar-right">
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Zurück</a>
    </div>
  </div>

  <c:if test="${not empty error}">
    <p class="error">${error}</p>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/teacher/quiz/builder">

    <div class="card">
      <label>Klasse auswählen:</label>
      <select name="classId" required>
        <c:forEach items="${classes}" var="k">
          <option value="${k.id}">${k.name}</option>
        </c:forEach>
      </select>

      <label>Thema:</label>
      <input name="title" required>
    </div>

    <div id="questions">
      <div class="card">
        <h3>Frage 1</h3>
        <label>Frage:</label>
        <textarea name="qText" required></textarea>

        <div class="two-col">
          <div>
            <label>Antwort A:</label>
            <input name="aText" required>
          </div>
          <div>
            <label>Antwort B:</label>
            <input name="bText" required>
          </div>
          <div>
            <label>Antwort C:</label>
            <input name="cText" required>
          </div>
          <div>
            <label>Antwort D:</label>
            <input name="dText" required>
          </div>
        </div>

        <label>Richtige Antwort:</label>
        <select name="correct">
          <option value="A">A</option>
          <option value="B">B</option>
          <option value="C">C</option>
          <option value="D">D</option>
        </select>
      </div>
    </div>

    <button class="btn secondary" type="button" onclick="addQuestion()">Weitere Frage hinzufügen</button>
    <button class="btn" type="submit">Quiz erstellen</button>
  </form>
</body>
</html>