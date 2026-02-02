<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Lehrer Login</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <h1>Lehrer Login</h1>

  <form method="post" action="${pageContext.request.contextPath}/teacher/login" class="card">
    <label>Anmeldename</label>
    <input name="username" required />

    <label>Passwort</label>
    <input name="password" type="password" required />

    <button class="btn" type="submit">Anmelden</button>

    <c:if test="${not empty error}">
      <p class="error">${error}</p>
    </c:if>
  </form>
</body>
</html>