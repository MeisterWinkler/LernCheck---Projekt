<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Quiz beitreten</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <h1>Quiz beitreten</h1>

  <c:if test="${param.done == '1'}">
    <p class="ok">Danke! Deine Abgabe wurde gespeichert.</p>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/student/join" class="card">
    <label>Einladungscode</label>
    <input name="code" required placeholder="z.B. SDG56" />

    <label>Dein Name</label>
    <input name="name" required placeholder="z.B. Max" />

    <button class="btn" type="submit">Beitreten</button>

    <c:if test="${not empty error}">
      <p class="error">${error}</p>
    </c:if>
  </form>
</body>
</html>