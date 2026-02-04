<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Template verwenden</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<div class="topbar">
  <h1>Template verwenden</h1>
  <div class="topbar-right">
    <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">Zurück</a>
  </div>
</div>

<c:if test="${not empty error}">
  <div class="card" style="margin-top:12px;">
    <p class="error">${error}</p>
  </div>
</c:if>

<c:if test="${empty templates}">
  <div class="card" style="margin-top:12px;">Keine Templates vorhanden.</div>
</c:if>

<c:forEach items="${templates}" var="t">
  <form class="card" method="post" action="${pageContext.request.contextPath}/teacher/templates"
        style="margin-top:12px; display:flex; justify-content:space-between; align-items:center; gap:16px;">

    <!-- ✅ WICHTIG: templateId muss gepostet werden -->
    <input type="hidden" name="templateId" value="${t.id}"/>

    <div>
      <div class="title" style="font-weight:700; font-size:18px;">${t.title}</div>
      <div class="muted">Erstellt: ${t.createdAt}</div>
    </div>

    <div style="display:flex; flex-direction:column; gap:10px; min-width:280px;">
      <select name="classId" required>
        <c:forEach items="${classes}" var="k">
          <option value="${k.id}">${k.name}</option>
        </c:forEach>
      </select>

      <button class="btn" type="submit">In Klasse hochladen</button>
    </div>
  </form>
</c:forEach>

</body>
</html>