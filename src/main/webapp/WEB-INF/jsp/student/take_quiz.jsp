<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!doctype html>
<html>
<head>
  <title>Quiz</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
  <script>
    // einfacher Live-Timer (optional)
    function startTimer(endsAtMillis) {
      const el = document.getElementById("timer");
      function tick() {
        const now = Date.now();
        let diff = Math.max(0, endsAtMillis - now);
        const min = Math.floor(diff / 60000);
        const sec = Math.floor((diff % 60000) / 1000);
        el.textContent = min + ":" + String(sec).padStart(2,"0");
        if (diff <= 0) {
          // Zeit abgelaufen -> optional auto-submit oder nur Anzeige
          // Hier lassen wir nur Anzeige
        } else {
          requestAnimationFrame(tick);
        }
      }
      tick();
    }
  </script>
</head>

<body class="container">

<div class="topbar">
  <h1>${quiz.title}</h1>
  <div class="topbar-right">
    <div class="badge">Zeit: <span id="timer">--:--</span></div>
  </div>
</div>

<script>
  <c:if test="${not empty endsAtMillis}">
    startTimer(${endsAtMillis});
  </c:if>
</script>

<form method="post" action="${pageContext.request.contextPath}/student/submit">

  <c:forEach items="${quiz.questions}" var="q">
    <div class="card" style="margin-top:16px;">
      <h3>Frage ${q.pos}: ${q.text}</h3>

      <c:forEach items="${q.options}" var="opt">
        <label style="display:block; margin: 6px 0;">
          <input type="radio"
                 name="answer_${q.id}"
                 value="${opt.key}">
          ${opt.key}: ${opt.value}
        </label>
      </c:forEach>
    </div>
  </c:forEach>

  <div class="card" style="margin-top:16px;">
    <h3>Optionales Feedback</h3>
    <textarea name="feedback" rows="4" style="width:100%;" placeholder="Was war unklar? Was war gut/schlecht? (optional)"></textarea>
  </div>

  <div style="margin-top:16px;">
    <button class="btn" type="submit">Abschicken</button>
  </div>

</form>

</body>
</html>