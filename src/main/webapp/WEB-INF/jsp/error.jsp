<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Hinweis</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <div class="card">
    <h1>Info</h1>
    <p>${message}</p>
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Zum Dashboard</a>
    <a class="btn secondary" href="${pageContext.request.contextPath}/student/join">Zur Schülerseite</a>
  </div>
</body>
</html>