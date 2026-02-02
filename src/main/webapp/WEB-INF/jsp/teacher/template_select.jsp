<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>Templates verwenden</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">
  <div class="topbar">
    <h1>Template verwenden</h1>
    <div class="topbar-right">
      <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Zurück</a>
    </div>
  </div>

  <c:if test="${empty templates}">
    <p>Noch keine Templates vorhanden. Erstelle zuerst ein Quiz.</p>
  </c:if>

  <c:forEach items="${templates}" var="t">
    <form class="card" method="post" action="${pageContext.request.contextPath}/teacher/templates">
      <div class="row">
        <div>
          <div class="title">${t.title}</div>
          <div class="muted">Erstellt: ${t.createdAt}</div>
        </div>

        <div class="row">
          <select name="classId" required>
            <c:forEach items="${classes}" var="k">
              <option value="${k.id}">${k.name}</option>
            </c:forEach>
          </select>
          <input type="hidden" name="templateId" value="${t.id}">
          <button class="btn" type="submit">In Klasse hochladen</button>
        </div>
      </div>
    </form>
  </c:forEach>
</body>
</html>