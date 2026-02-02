<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Auswertung</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body class="container">
  <div class="topbar">
    <h1>Auswertung</h1>
    <div class="topbar-right">
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/quiz/start?id=${quizId}">Zurück</a>
      <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/feedback?id=${quizId}">Feedback</a>
    </div>
  </div>

  <c:if test="${endedAt != null}">
    <p class="muted">Durchgeführt am: <b>${endedAt}</b></p>
  </c:if>

  <div class="card">
    <canvas id="bar"></canvas>
  </div>

  <c:forEach items="${rows}" var="r">
    <div class="card">
      <div class="title">Frage ${r.pos}: ${r.questionText}</div>
      <c:choose>
        <c:when test="${r.percentCorrect < 50}">
          <hr class="bad">
        </c:when>
        <c:otherwise>
          <hr class="good">
        </c:otherwise>
      </c:choose>
      <div><b><c:out value="${r.percentCorrect}"/></b>% richtig</div>
    </div>
  </c:forEach>

  <script>
    const labels = [
      <c:forEach items="${rows}" var="r" varStatus="s">
        "Frage ${r.pos}"<c:if test="${!s.last}">,</c:if>
      </c:forEach>
    ];
    const data = [
      <c:forEach items="${rows}" var="r" varStatus="s">
        ${r.percentCorrect}<c:if test="${!s.last}">,</c:if>
      </c:forEach>
    ];

    new Chart(document.getElementById("bar"), {
      type: 'bar',
      data: { labels, datasets: [{ label: '% richtig', data }] },
      options: { scales: { y: { beginAtZero: true, max: 100 } } }
    });
  </script>
</body>
</html>