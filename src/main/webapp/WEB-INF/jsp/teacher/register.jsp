<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Lehrer registrieren</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<h1>Lehrer Registrierung</h1>

<form method="post" action="${pageContext.request.contextPath}/teacher/register" class="card">

  <label>Benutzername</label>
  <input name="username" required>

  <label>Passwort</label>
  <input name="password" type="password" required>

  <button class="btn" type="submit">Registrieren</button>

  <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/login">
    Zurück zum Login
  </a>

  <c:if test="${not empty error}">
    <p class="error">${error}</p>
  </c:if>

</form>

</body>
</html>