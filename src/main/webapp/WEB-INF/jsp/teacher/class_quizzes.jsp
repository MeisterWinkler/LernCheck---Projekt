<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Quizze der Klasse</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <div class="topbar">
    <h1>${klass.name} – Quizze</h1>
    <div class="topbar-right">
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Zurück</a>
    </div>
  </div>

  <c:if test="${empty quizzes}">
    <p>Noch keine Quizze für diese Klasse.</p>
  </c:if>

  <c:forEach items="${quizzes}" var="q">
    <div class="card">
      <div class="row">
        <div>
          <div class="title">${q.title}</div>
          <div class="muted">
            Status: <b>${q.status}</b>
            <c:if test="${q.startedAt != null}">
              | gestartet: ${q.startedAt}
            </c:if>
            <c:if test="${q.endedAt != null}">
              | beendet: ${q.endedAt}
            </c:if>
          </div>
        </div>

        <div>
          <c:choose>
            <c:when test="${q.status == 'NOT_STARTED' || q.status == 'RUNNING'}">
              <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/start?id=${q.quizId}">Öffnen</a>
            </c:when>
            <c:otherwise>
              <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/results?id=${q.quizId}">Auswertung</a>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </c:forEach>
</body>
</html>