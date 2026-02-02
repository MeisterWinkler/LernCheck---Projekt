<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Klasse erstellen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<h1>Neue Klasse erstellen</h1>

<form method="post" action="${pageContext.request.contextPath}/teacher/class/create" class="card">

  <label>Klassenname (z.B. FIA23A)</label>
  <input name="name" required>

  <button class="btn" type="submit">Speichern</button>

  <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">
    Abbrechen
  </a>

  <c:if test="${not empty error}">
    <p class="error">${error}</p>
  </c:if>

</form>

</body>
</html>