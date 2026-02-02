<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Info</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
</head>
<body class="container">

<div class="card">
  <h1>Info</h1>

  <p>
    <c:choose>
      <c:when test="${not empty message}">
        ${message}
      </c:when>
      <c:otherwise>
        Es ist ein Fehler aufgetreten.
      </c:otherwise>
    </c:choose>
  </p>

  <div style="margin-top: 16px; display:flex; gap:10px; flex-wrap:wrap;">
    <c:choose>
      <c:when test="${showStudentOnly == true}">
        <a class="btn secondary" href="${pageContext.request.contextPath}/student/join">
          Zur Schülerseite
        </a>
      </c:when>

      <c:when test="${showTeacherLinks == true}">
        <a class="btn secondary" href="${pageContext.request.contextPath}/teacher/dashboard">
          Zum Dashboard
        </a>
        <a class="btn secondary" href="${pageContext.request.contextPath}/student/join">
          Zur Schülerseite
        </a>
      </c:when>

      <c:otherwise>
        <a class="btn secondary" href="${pageContext.request.contextPath}/student/join">
          Zur Schülerseite
        </a>
      </c:otherwise>
    </c:choose>
  </div>

</div>

</body>
</html>