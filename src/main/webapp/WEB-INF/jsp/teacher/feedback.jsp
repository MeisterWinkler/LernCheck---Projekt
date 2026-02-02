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
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/quiz/results?id=${quizId}">Zurück</a>
    </div>
  </div>

  <c:if test="${empty feedback}">
    <p>Kein Feedback abgegeben.</p>
  </c:if>

  <c:forEach items="${feedback}" var="f" varStatus="s">
    <div class="card">
      <div class="title">Feedback ${s.index + 1}</div>
      <div>${f}</div>
    </div>
  </c:forEach>
</body>
</html>