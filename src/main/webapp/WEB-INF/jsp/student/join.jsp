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

<div class="card">
  <form method="post" action="${pageContext.request.contextPath}/student/join">

    <label>Einladungscode</label>
    <input name="code" required>

    <button class="btn" type="submit" style="margin-top:10px;">Beitreten</button>

    <c:if test="${not empty error}">
      <p class="error">${error}</p>
    </c:if>

    <c:if test="${param.done == '1'}">
      <p class="ok">Abgabe erfolgreich.</p>
    </c:if>

  </form>
</div>

</body>
</html>