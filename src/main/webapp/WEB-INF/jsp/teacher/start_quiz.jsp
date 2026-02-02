<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Quiz</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<div class="topbar">
  <h1>${quiz.title}</h1>
  <div class="topbar-right">
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Dashboard</a>

    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/quiz/feedback?id=${quiz.quizId}">
      Feedback
    </a>

    <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/results?id=${quiz.quizId}">
      Auswertung
    </a>

    <form method="post" action="${pageContext.request.contextPath}/teacher/quiz/restart" style="display:inline;">
      <input type="hidden" name="id" value="${quiz.quizId}">
      <button class="btn danger" type="submit">Quiz neu starten</button>
    </form>
  </div>
</div>

<div class="card">
  <p><b>Status:</b> ${quiz.status}</p>
  <p><b>Einladungscode:</b> <span style="font-weight:700;">${quiz.inviteCode}</span></p>
  <p><b>Dauer:</b>
    <c:choose>
      <c:when test="${empty quiz.durationSeconds}">-</c:when>
      <c:otherwise>${quiz.durationSeconds/60.0} Minuten</c:otherwise>
    </c:choose>
  </p>
  <p><b>Läuft bis:</b> ${quiz.endsAt}</p>
  <p><b>Schüler-Link:</b> ${pageContext.request.contextPath}/student/join</p>
</div>

<c:if test="${quiz.status eq 'NOT_STARTED'}">
  <div class="card">
    <h2>Quiz starten</h2>
    <form method="post" action="${pageContext.request.contextPath}/teacher/quiz/start">
      <input type="hidden" name="id" value="${quiz.quizId}">
      <label>Dauer (in Minuten)</label>
      <input type="number" name="durationMinutes" min="1" max="180" value="10" required>
      <button class="btn" type="submit">Starten</button>

      <c:if test="${not empty error}">
        <p class="error">${error}</p>
      </c:if>
    </form>
  </div>
</c:if>

<div class="card">
  <h2>Fragen</h2>

  <c:forEach items="${quiz.questions}" var="q">
    <div style="margin: 14px 0; padding: 12px; border: 1px solid #e5e7eb; border-radius: 12px;">
      <div style="font-weight:700;">Frage ${q.pos}: ${q.text}</div>

      <div style="margin-top:8px;">
        <c:forEach items="${q.options}" var="opt">
          <div style="margin:4px 0;">
            <c:choose>
              <c:when test="${opt.key == q.correct}">
                <span style="font-weight:700; color:#16a34a;">${opt.key}: ${opt.value} (richtig)</span>
              </c:when>
              <c:otherwise>
                <span>${opt.key}: ${opt.value}</span>
              </c:otherwise>
            </c:choose>
          </div>
        </c:forEach>
      </div>

      <div class="muted" style="margin-top:8px;">Richtig: ${q.correct}</div>
    </div>
  </c:forEach>
</div>

</body>
</html>