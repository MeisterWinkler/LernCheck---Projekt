<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <title>${quiz.title}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css">
  <script>
    function pad(n){ return (n<10?'0':'')+n; }

    // endsAt kommt als String "YYYY-MM-DDTHH:MM:SS" (LocalDateTime). Wir bauen daraus ein Date.
    const endsAtStr = "${endsAt}";
    let endsAt = null;

    function parseLocalDateTime(s){
      // "2026-02-02T12:34:56"
      const [date, time] = s.split('T');
      const [y,m,d] = date.split('-').map(Number);
      const [hh,mm,ss] = time.split(':').map(Number);
      return new Date(y, m-1, d, hh, mm, ss || 0);
    }

    function tick(){
      if (!endsAt) return;
      const now = new Date();
      let diff = Math.floor((endsAt.getTime() - now.getTime()) / 1000);
      if (diff < 0) diff = 0;

      const mm = Math.floor(diff / 60);
      const ss = diff % 60;
      document.getElementById("timer").innerText = pad(mm) + ":" + pad(ss);

      if (diff === 0) {
        // Auto-Submit: letzter Stand wird gespeichert
        document.getElementById("submitBtn").disabled = true;
        document.getElementById("autoMsg").style.display = "block";
        document.getElementById("quizForm").submit();
      }
    }

    window.addEventListener("load", () => {
      endsAt = parseLocalDateTime(endsAtStr);
      tick();
      setInterval(tick, 500);
    });
  </script>
</head>
<body class="container">

  <div class="topbar">
    <h1>${quiz.title}</h1>
    <div class="timerbox">⏱ <span id="timer">--:--</span></div>
  </div>

  <form id="quizForm" method="post" action="${pageContext.request.contextPath}/student/submit">
    <c:forEach items="${quiz.questions}" var="q">
      <div class="card">
        <div class="title">Frage ${q.pos}: ${q.text}</div>

        <div class="options">
          <c:forEach items="${q.options}" var="opt">
            <label class="opt">
              <input type="radio" name="q_${q.id}" value="${opt.key}">
              <b>${opt.key}:</b> ${opt.value}
            </label>
          </c:forEach>
        </div>
      </div>
    </c:forEach>

    <div class="card">
      <h3>Feedback (Optional)</h3>
      <textarea name="feedback" placeholder="Schreibe optional Feedback..."></textarea>
    </div>

    <p id="autoMsg" class="muted" style="display:none;">
      Zeit ist abgelaufen – Abgabe wird automatisch gesendet…
    </p>

    <button id="submitBtn" class="btn" type="submit">Abschicken</button>
  </form>
</body>
</html>