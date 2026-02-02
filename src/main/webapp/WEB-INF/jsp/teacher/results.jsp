<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

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
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Dashboard</a>
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/quiz/start?id=${quizId}">Zum Quiz</a>
  </div>
</div>

<c:if test="${not empty endedAt}">
  <p class="muted">Durchgeführt am: ${endedAt}</p>
</c:if>

<div class="card">
  <canvas id="chart" height="90"></canvas>
</div>

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

  new Chart(document.getElementById('chart'), {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: '% richtig',
        data
      }]
    },
    options: {
      scales: { y: { beginAtZero: true, max: 100 } }
    }
  });
</script>

<c:forEach items="${rows}" var="r">
  <div class="card" style="margin-top: 16px;">
    <h3>Frage ${r.pos}: ${r.questionText}</h3>

    <c:choose>
      <c:when test="${r.percentCorrect lt 50}">
        <div style="height:6px;background:#ef4444;border-radius:4px;margin:10px 0;"></div>
      </c:when>
      <c:otherwise>
        <div style="height:6px;background:#22c55e;border-radius:4px;margin:10px 0;"></div>
      </c:otherwise>
    </c:choose>

    <div>
      <fmt:formatNumber value="${r.percentCorrect}" maxFractionDigits="2" minFractionDigits="2" />% richtig
    </div>
  </div>
</c:forEach>

<!-- ✅ Feedback direkt in der Auswertung -->
<div class="card" style="margin-top: 18px;">
  <h2>Feedback</h2>

  <c:if test="${empty feedback}">
    <p class="muted">Kein Feedback abgegeben.</p>
  </c:if>

  <c:forEach items="${feedback}" var="f">
    <div style="padding:10px;border:1px solid #e5e7eb;border-radius:10px;margin:10px 0;">
      ${f}
    </div>
  </c:forEach>
</div>

</body>
</html>