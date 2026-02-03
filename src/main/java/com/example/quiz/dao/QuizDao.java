package com.example.quiz.dao;

import com.example.quiz.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class QuizDao {

    // ============ AUTO-END ============
    public void endAllExpired(Connection c) throws Exception {
        String sql = """
          UPDATE class_quizzes
          SET status='ENDED', ended_at=NOW()
          WHERE status='RUNNING' AND ends_at IS NOT NULL AND ends_at <= NOW()
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // ============ TEMPLATES ============
    public long createTemplate(Connection c, long teacherId, String title) throws Exception {
        String sql = "INSERT INTO quiz_templates(teacher_id, title) VALUES (?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, teacherId);
            ps.setString(2, title);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    public long createTemplateQuestion(Connection c, long templateId, String text, char correct, int pos) throws Exception {
        String sql = "INSERT INTO template_questions(template_id, question_text, correct_option, pos) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, templateId);
            ps.setString(2, text);
            ps.setString(3, String.valueOf(correct));
            ps.setInt(4, pos);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    public void upsertOption(Connection c, long questionId, char letter, String text) throws Exception {
        String sql = """
          INSERT INTO template_options(question_id, option_letter, option_text)
          VALUES (?,?,?)
          ON DUPLICATE KEY UPDATE option_text=VALUES(option_text)
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            ps.setString(2, String.valueOf(letter));
            ps.setString(3, text);
            ps.executeUpdate();
        }
    }

    public List<TemplateSummary> listTemplates(Connection c, long teacherId) throws Exception {
        String sql = "SELECT id, title, created_at FROM quiz_templates WHERE teacher_id=? ORDER BY created_at DESC";
        List<TemplateSummary> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TemplateSummary t = new TemplateSummary();
                    t.setId(rs.getLong("id"));
                    t.setTitle(rs.getString("title"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    t.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    out.add(t);
                }
            }
        }
        return out;
    }

    public Quiz loadTemplate(Connection c, long templateId) throws Exception {
        Quiz quiz = new Quiz();
        quiz.setTemplateId(templateId);

        try (PreparedStatement ps = c.prepareStatement("SELECT title FROM quiz_templates WHERE id=?")) {
            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                quiz.setTitle(rs.getString("title"));
            }
        }

        List<QuizQuestion> base = loadQuestionsByTemplateId(c, templateId);
        quiz.setQuestions(base);
        return quiz;
    }

    // ============ CLASS QUIZZES ============
    public long createClassQuizFromTemplate(Connection c, long teacherId, long classId, long templateId) throws Exception {
        String sql = "INSERT INTO class_quizzes(teacher_id, class_id, template_id) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, classId);
            ps.setLong(3, templateId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    public List<Map<String, Object>> listQuizzesForTeacherAndClass(Connection c, long teacherId, long classId) throws Exception {
        endAllExpired(c);

        String sql = """
          SELECT cq.id as quiz_id, qt.title, cq.status, cq.created_at, cq.started_at, cq.ended_at
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id = cq.template_id
          WHERE cq.teacher_id=? AND cq.class_id=?
          ORDER BY cq.created_at DESC
        """;
        List<Map<String,Object>> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new HashMap<>();
                    row.put("quizId", rs.getLong("quiz_id"));
                    row.put("title", rs.getString("title"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("startedAt", rs.getTimestamp("started_at"));
                    row.put("endedAt", rs.getTimestamp("ended_at"));
                    out.add(row);
                }
            }
        }
        return out;
    }

    // ============ START QUIZ + WEAK INJECTION ============
    public static class WeakSourceQuiz {
        private long id;
        private String title;
        private LocalDateTime endedAt;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public LocalDateTime getEndedAt() { return endedAt; }
        public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    }

    public List<WeakSourceQuiz> listLast3WeakSourceQuizzes(Connection c, long teacherId, long currentQuizId) throws Exception {
        endAllExpired(c);

        // Wir nehmen die letzten 3 ENDED Quizzes des Lehrers (außer dem aktuellen)
        // die überhaupt weak_questions haben.
        String sql = """
          SELECT cq.id, qt.title, cq.ended_at
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id = cq.template_id
          WHERE cq.teacher_id=?
            AND cq.status='ENDED'
            AND cq.id <> ?
            AND EXISTS (SELECT 1 FROM weak_questions wq WHERE wq.quiz_id = cq.id)
          ORDER BY cq.ended_at DESC
          LIMIT 3
        """;
        List<WeakSourceQuiz> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, currentQuizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WeakSourceQuiz w = new WeakSourceQuiz();
                    w.setId(rs.getLong("id"));
                    w.setTitle(rs.getString("title"));
                    Timestamp t = rs.getTimestamp("ended_at");
                    w.setEndedAt(t != null ? t.toLocalDateTime() : null);
                    out.add(w);
                }
            }
        }
        return out;
    }

    public void copyWeakQuestionsToQuiz(Connection c, long teacherId, long targetQuizId, long sourceQuizId) throws Exception {
        // Guard: source must belong to same teacher and be ENDED
        try (PreparedStatement guard = c.prepareStatement("""
            SELECT 1 FROM class_quizzes WHERE id=? AND teacher_id=? AND status='ENDED'
        """)) {
            guard.setLong(1, sourceQuizId);
            guard.setLong(2, teacherId);
            try (ResultSet rs = guard.executeQuery()) {
                if (!rs.next()) return;
            }
        }

        // Kopieren (idempotent über uq_extra)
        String sql = """
          INSERT INTO class_quiz_extra_questions(quiz_id, question_id, source_quiz_id)
          SELECT ?, wq.question_id, wq.quiz_id
          FROM weak_questions wq
          WHERE wq.quiz_id = ?
          ON DUPLICATE KEY UPDATE source_quiz_id=VALUES(source_quiz_id)
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, targetQuizId);
            ps.setLong(2, sourceQuizId);
            ps.executeUpdate();
        }
    }

    public void startQuiz(Connection c, long quizId, long teacherId, int durationSeconds, String inviteCode) throws Exception {
        String sql = """
          UPDATE class_quizzes
          SET status='RUNNING',
              invite_code=?,
              duration_seconds=?,
              started_at=NOW(),
              ends_at=DATE_ADD(NOW(), INTERVAL ? SECOND),
              ended_at=NULL
          WHERE id=? AND teacher_id=?
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, inviteCode);
            ps.setInt(2, durationSeconds);
            ps.setInt(3, durationSeconds);
            ps.setLong(4, quizId);
            ps.setLong(5, teacherId);
            ps.executeUpdate();
        }
    }

    // ============ LOAD QUIZ (Teacher) ============
    public Quiz loadClassQuiz(Connection c, long quizId, long teacherId) throws Exception {
        endAllExpired(c);

        String sql = """
          SELECT cq.id, cq.template_id, cq.status, cq.invite_code, cq.duration_seconds,
                 cq.started_at, cq.ends_at, cq.ended_at, cq.class_id, qt.title
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id=cq.template_id
          WHERE cq.id=? AND cq.teacher_id=?
        """;

        Quiz quiz;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                quiz = new Quiz();
                quiz.setQuizId(rs.getLong("id"));
                quiz.setTemplateId(rs.getLong("template_id"));
                quiz.setStatus(rs.getString("status"));
                quiz.setInviteCode(rs.getString("invite_code"));

                int dur = rs.getInt("duration_seconds");
                quiz.setDurationSeconds(rs.wasNull() ? null : dur);

                Timestamp st = rs.getTimestamp("started_at");
                Timestamp en = rs.getTimestamp("ends_at");
                Timestamp ed = rs.getTimestamp("ended_at");

                quiz.setStartedAt(st != null ? st.toLocalDateTime() : null);
                quiz.setEndsAt(en != null ? en.toLocalDateTime() : null);
                quiz.setEndedAt(ed != null ? ed.toLocalDateTime() : null);

                quiz.setTitle(rs.getString("title"));
            }
        }

        // Base + Extras mergen
        List<QuizQuestion> base = loadQuestionsByTemplateId(c, quiz.getTemplateId());
        List<QuizQuestion> extras = loadExtraQuestionsForQuiz(c, quiz.getQuizId());

        quiz.setQuestions(mergeQuestions(base, extras));
        return quiz;
    }

    // ============ LOAD QUIZ (Student by code) ============
    public Quiz loadClassQuizForStudentByCode(Connection c, String inviteCode) throws Exception {
        endAllExpired(c);

        // falls abgelaufen: ENDED setzen
        try (PreparedStatement ps = c.prepareStatement("""
          UPDATE class_quizzes
          SET status='ENDED', ended_at=NOW()
          WHERE invite_code=? AND status='RUNNING' AND ends_at IS NOT NULL AND ends_at <= NOW()
        """)) {
            ps.setString(1, inviteCode);
            ps.executeUpdate();
        }

        String sql = """
          SELECT cq.id, cq.template_id, cq.status, cq.invite_code, cq.duration_seconds,
                 cq.started_at, cq.ends_at, qt.title
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id=cq.template_id
          WHERE cq.invite_code=?
        """;

        Quiz quiz;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, inviteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!"RUNNING".equals(rs.getString("status"))) return null;

                Timestamp endsAt = rs.getTimestamp("ends_at");
                if (endsAt != null && endsAt.toLocalDateTime().isBefore(LocalDateTime.now())) return null;

                quiz = new Quiz();
                quiz.setQuizId(rs.getLong("id"));
                quiz.setTemplateId(rs.getLong("template_id"));
                quiz.setStatus(rs.getString("status"));
                quiz.setInviteCode(rs.getString("invite_code"));
                quiz.setDurationSeconds(rs.getInt("duration_seconds"));

                Timestamp st = rs.getTimestamp("started_at");
                quiz.setStartedAt(st != null ? st.toLocalDateTime() : null);
                quiz.setEndsAt(endsAt != null ? endsAt.toLocalDateTime() : null);

                quiz.setTitle(rs.getString("title"));
            }
        }

        List<QuizQuestion> base = loadQuestionsByTemplateId(c, quiz.getTemplateId());
        List<QuizQuestion> extras = loadExtraQuestionsForQuiz(c, quiz.getQuizId());
        quiz.setQuestions(mergeQuestions(base, extras));
        return quiz;
    }

    // ============ ATTEMPTS / ANSWERS ============
    public long createAttempt(Connection c, long quizId, long studentId, String feedback) throws Exception {
        String sql = "INSERT INTO quiz_attempts(quiz_id, student_id, submitted_at, feedback) VALUES (?,?,NOW(),?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, quizId);
            ps.setLong(2, studentId);
            ps.setString(3, feedback);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    public void insertAnswer(Connection c, long attemptId, long questionId, char chosen) throws Exception {
        String sql = "INSERT INTO attempt_answers(attempt_id, question_id, chosen_option) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, attemptId);
            ps.setLong(2, questionId);
            ps.setString(3, String.valueOf(chosen));
            ps.executeUpdate();
        }
    }

    // ============ RESULTS + WEAK STORE ============
    public LocalDateTime getEndedAt(Connection c, long quizId, long teacherId) throws Exception {
        endAllExpired(c);
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT ended_at FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            ps.setLong(1, quizId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp t = rs.getTimestamp("ended_at");
                return t == null ? null : t.toLocalDateTime();
            }
        }
    }

    public List<QuizResultRow> computeResults(Connection c, long quizId, long teacherId) throws Exception {
        endAllExpired(c);

        // guard
        try (PreparedStatement guard = c.prepareStatement("SELECT 1 FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            guard.setLong(1, quizId);
            guard.setLong(2, teacherId);
            try (ResultSet rs = guard.executeQuery()) {
                if (!rs.next()) return List.of();
            }
        }

        String sql = """
          SELECT tq.id AS question_id, tq.pos, tq.question_text,
                 SUM(CASE WHEN aa.chosen_option = tq.correct_option THEN 1 ELSE 0 END) AS correct_cnt,
                 COUNT(aa.id) AS total_cnt
          FROM class_quizzes cq
          JOIN template_questions tq ON tq.template_id = cq.template_id
          LEFT JOIN quiz_attempts qa ON qa.quiz_id = cq.id
          LEFT JOIN attempt_answers aa ON aa.attempt_id = qa.id AND aa.question_id = tq.id
          WHERE cq.id=?
          GROUP BY tq.id, tq.pos, tq.question_text
          ORDER BY tq.pos
        """;

        List<QuizResultRow> rows = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizResultRow r = new QuizResultRow();
                    r.setQuestionId(rs.getLong("question_id"));
                    r.setPos(rs.getInt("pos"));
                    r.setQuestionText(rs.getString("question_text"));

                    int correct = rs.getInt("correct_cnt");
                    int total = rs.getInt("total_cnt");
                    r.setPercentCorrect(total == 0 ? 0.0 : (100.0 * correct / total));
                    rows.add(r);
                }
            }
        }

        // ✅ Weak-Algorithmus speichern (≤ 50%)
        storeWeakQuestions(c, quizId, teacherId);

        return rows;
    }

    private void storeWeakQuestions(Connection c, long quizId, long teacherId) throws Exception {
        // Nur speichern, wenn das Quiz ENDED ist
        try (PreparedStatement st = c.prepareStatement("SELECT status FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            st.setLong(1, quizId);
            st.setLong(2, teacherId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return;
                if (!"ENDED".equals(rs.getString("status"))) return;
            }
        }

        // Insert-Select: percent_correct berechnen und <=50 speichern
        String sql = """
          INSERT INTO weak_questions(teacher_id, quiz_id, question_id, percent_correct)
          SELECT ?, cq.id, tq.id,
                 ROUND(
                   CASE WHEN COUNT(aa.id)=0 THEN 0
                        ELSE 100.0 * SUM(CASE WHEN aa.chosen_option = tq.correct_option THEN 1 ELSE 0 END) / COUNT(aa.id)
                   END
                 , 2) AS pct
          FROM class_quizzes cq
          JOIN template_questions tq ON tq.template_id = cq.template_id
          LEFT JOIN quiz_attempts qa ON qa.quiz_id = cq.id
          LEFT JOIN attempt_answers aa ON aa.attempt_id = qa.id AND aa.question_id = tq.id
          WHERE cq.id=?
          GROUP BY cq.id, tq.id
          HAVING pct <= 50
          ON DUPLICATE KEY UPDATE percent_correct=VALUES(percent_correct), created_at=CURRENT_TIMESTAMP
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, quizId);
            ps.executeUpdate();
        }
    }

    public List<String> listFeedback(Connection c, long quizId, long teacherId) throws Exception {
        endAllExpired(c);

        try (PreparedStatement guard = c.prepareStatement("SELECT 1 FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            guard.setLong(1, quizId);
            guard.setLong(2, teacherId);
            try (ResultSet rs = guard.executeQuery()) {
                if (!rs.next()) return List.of();
            }
        }

        String sql = """
          SELECT feedback
          FROM quiz_attempts
          WHERE quiz_id=? AND feedback IS NOT NULL AND TRIM(feedback)<>'' 
          ORDER BY submitted_at DESC
        """;
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("feedback"));
            }
        }
        return out;
    }

    // ============ RESTART ============
    public void restartQuiz(Connection c, long quizId, long teacherId) throws Exception {
        try (PreparedStatement guard = c.prepareStatement("SELECT 1 FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            guard.setLong(1, quizId);
            guard.setLong(2, teacherId);
            try (ResultSet rs = guard.executeQuery()) {
                if (!rs.next()) return;
            }
        }

        try (PreparedStatement ps1 = c.prepareStatement(
                "DELETE aa FROM attempt_answers aa JOIN quiz_attempts qa ON qa.id=aa.attempt_id WHERE qa.quiz_id=?")) {
            ps1.setLong(1, quizId);
            ps1.executeUpdate();
        }

        try (PreparedStatement ps2 = c.prepareStatement("DELETE FROM quiz_attempts WHERE quiz_id=?")) {
            ps2.setLong(1, quizId);
            ps2.executeUpdate();
        }

        // Extra-Fragen zurücksetzen
        try (PreparedStatement psx = c.prepareStatement("DELETE FROM class_quiz_extra_questions WHERE quiz_id=?")) {
            psx.setLong(1, quizId);
            psx.executeUpdate();
        }

        try (PreparedStatement ps3 = c.prepareStatement("""
            UPDATE class_quizzes
            SET status='NOT_STARTED',
                invite_code=NULL,
                duration_seconds=NULL,
                started_at=NULL,
                ends_at=NULL,
                ended_at=NULL
            WHERE id=? AND teacher_id=?
        """)) {
            ps3.setLong(1, quizId);
            ps3.setLong(2, teacherId);
            ps3.executeUpdate();
        }
    }

    // ============ HELPERS: load questions ============
    private List<QuizQuestion> loadQuestionsByTemplateId(Connection c, long templateId) throws Exception {
        String qSql = "SELECT id, question_text, correct_option, pos FROM template_questions WHERE template_id=? ORDER BY pos";
        Map<Long, QuizQuestion> qMap = new LinkedHashMap<>();

        try (PreparedStatement ps = c.prepareStatement(qSql)) {
            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizQuestion q = new QuizQuestion();
                    q.setId(rs.getLong("id"));
                    q.setText(rs.getString("question_text"));
                    q.setCorrect(rs.getString("correct_option").charAt(0));
                    q.setPos(rs.getInt("pos"));
                    qMap.put(q.getId(), q);
                }
            }
        }

        loadOptionsForQuestionMap(c, qMap);
        return new ArrayList<>(qMap.values());
    }

    private List<QuizQuestion> loadExtraQuestionsForQuiz(Connection c, long quizId) throws Exception {
        String sql = """
          SELECT tq.id, tq.question_text, tq.correct_option
          FROM class_quiz_extra_questions eq
          JOIN template_questions tq ON tq.id = eq.question_id
          WHERE eq.quiz_id=?
          ORDER BY eq.id
        """;
        Map<Long, QuizQuestion> qMap = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                int i = 1;
                while (rs.next()) {
                    QuizQuestion q = new QuizQuestion();
                    q.setId(rs.getLong("id"));
                    q.setText(rs.getString("question_text"));
                    q.setCorrect(rs.getString("correct_option").charAt(0));
                    q.setPos(i++); // temporär; wird beim merge neu gesetzt
                    qMap.put(q.getId(), q);
                }
            }
        }
        loadOptionsForQuestionMap(c, qMap);
        return new ArrayList<>(qMap.values());
    }

    private void loadOptionsForQuestionMap(Connection c, Map<Long, QuizQuestion> qMap) throws Exception {
        if (qMap.isEmpty()) return;

        StringBuilder in = new StringBuilder();
        for (Long id : qMap.keySet()) {
            if (!in.isEmpty()) in.append(",");
            in.append(id);
        }

        String oSql = "SELECT question_id, option_letter, option_text FROM template_options WHERE question_id IN (" + in + ")";
        try (PreparedStatement ps = c.prepareStatement(oSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long qid = rs.getLong("question_id");
                char letter = rs.getString("option_letter").charAt(0);
                String text = rs.getString("option_text");
                QuizQuestion q = qMap.get(qid);
                if (q != null) q.getOptions().put(letter, text);
            }
        }
    }

    private List<QuizQuestion> mergeQuestions(List<QuizQuestion> base, List<QuizQuestion> extras) {
        List<QuizQuestion> out = new ArrayList<>();
        out.addAll(base);

        int maxPos = 0;
        for (QuizQuestion q : base) maxPos = Math.max(maxPos, q.getPos());

        // Extra an das Ende, Position fortlaufend
        for (QuizQuestion q : extras) {
            q.setPos(++maxPos);
            out.add(q);
        }

        return out;
    }
}