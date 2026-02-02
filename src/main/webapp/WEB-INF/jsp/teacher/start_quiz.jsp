<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Quiz starten</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <div class="topbar">
    <h1>${quiz.title}</h1>
    <div class="topbar-right">
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Dashboard</a>

      <form method="post" action="${pageContext.request.contextPath}/teacher/quiz/restart?id=${quiz.quizId}" style="display:inline;">
        <button class="btn danger" type="submit">Quiz neu starten</button>
      </form>

      <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/results?id=${quiz.quizId}">Auswertung</a>
    </div>
  </div>

  <div class="card">
    <p>Status: <b>${quiz.status}</b></p>

    <c:if test="${quiz.status == 'RUNNING'}">
      <p>Einladungscode: <b>${quiz.inviteCode}</b></p>
      <p>Dauer: <b>${quiz.durationSeconds/60}</b> Minuten</p>
      <p>Läuft bis: <b>${quiz.endsAt}</b></p>
      <p>Schüler-Link: <code>${pageContext.request.contextPath}/student/join</code></p>
    </c:if>

    <c:if test="${quiz.status != 'RUNNING'}">
      <form method="post" action="${pageContext.request.contextPath}/teacher/quiz/start">
        <input type="hidden" name="id" value="${quiz.quizId}">
        <label>Dauer (Minuten):</label>
        <select name="durationMin">
          <option>5</option>
          <option selected>10</option>
          <option>15</option>
          <option>20</option>
        </select>
        <button class="btn" type="submit">Starten</button>
      </form>
    </c:if>

    <c:if test="${quiz.status == 'ENDED'}">
      <p>Durchgeführt am: <b>${quiz.endedAt}</b></p>
    </c:if>
  </div>

  <div class="card">
    <h2>Fragen</h2>
    <c:forEach items="${quiz.questions}" var="q">
      <div class="qblock">
        <div class="title">Frage ${q.pos}: ${q.text}</div>
        <div class="muted">Richtig: ${q.correct}</div>
      </div>
    </c:forEach>
  </div>
</body>
</html>