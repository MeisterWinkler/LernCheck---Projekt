<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Feedback</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<div class="topbar">
  <h1>Feedback</h1>
  <div class="topbar-right">
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/quiz/results?id=${quizId}">Zur Auswertung</a>
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Dashboard</a>
  </div>
</div>

<c:if test="${empty feedback}">
  <div class="card">
    <p>Kein Feedback abgegeben.</p>
  </div>
</c:if>

<c:forEach items="${feedback}" var="f">
  <div class="card" style="margin-top:12px;">
    ${f}
  </div>
</c:forEach>

</body>
</html>