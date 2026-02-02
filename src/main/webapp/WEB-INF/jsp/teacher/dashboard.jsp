<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Dashboard Lehrer</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <div class="topbar">
    <h1>Dashboard Lehrer</h1>

    <div class="topbar-right">
      <a class="btn" href="${pageContext.request.contextPath}/teacher/quiz/builder">Quiz erstellen</a>
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/templates">Template verwenden</a>

      <form method="post" action="${pageContext.request.contextPath}/teacher/logout" style="display:inline;">
        <button class="btn danger" type="submit">Logout</button>
      </form>
    </div>
  </div>

  <div class="grid">
    <c:forEach items="${classes}" var="k">
      <a class="tile" href="${pageContext.request.contextPath}/teacher/class?id=${k.id}">
        ${k.name}
      </a>
    </c:forEach>
  </div>
</body>
</html>