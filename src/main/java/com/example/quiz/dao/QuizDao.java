package com.example.quiz.dao;

import com.example.quiz.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class QuizDao {

    // -------- Templates --------
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
                    t.id = rs.getLong("id");
                    t.title = rs.getString("title");
                    Timestamp ts = rs.getTimestamp("created_at");
                    t.createdAt = ts != null ? ts.toLocalDateTime() : null;
                    out.add(t);
                }
            }
        }
        return out;
    }

    public Quiz loadTemplate(Connection c, long templateId) throws Exception {
        Quiz quiz = new Quiz();
        quiz.templateId = templateId;

        try (PreparedStatement ps = c.prepareStatement("SELECT title FROM quiz_templates WHERE id=?")) {
            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                quiz.title = rs.getString("title");
            }
        }

        String qSql = "SELECT id, question_text, correct_option, pos FROM template_questions WHERE template_id=? ORDER BY pos";
        Map<Long, QuizQuestion> qMap = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(qSql)) {
            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizQuestion q = new QuizQuestion();
                    q.id = rs.getLong("id");
                    q.text = rs.getString("question_text");
                    q.correct = rs.getString("correct_option").charAt(0);
                    q.pos = rs.getInt("pos");
                    qMap.put(q.id, q);
                }
            }
        }

        String oSql = "SELECT question_id, option_letter, option_text FROM template_options WHERE question_id IN (" +
                (qMap.isEmpty() ? "NULL" : qMap.keySet().toString().replace("[", "").replace("]", "")) + ")";

        if (!qMap.isEmpty()) {
            try (PreparedStatement ps = c.prepareStatement(oSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long qid = rs.getLong("question_id");
                    char letter = rs.getString("option_letter").charAt(0);
                    String text = rs.getString("option_text");
                    qMap.get(qid).options.put(letter, text);
                }
            }
        }

        quiz.questions.addAll(qMap.values());
        return quiz;
    }

    // -------- Class quiz instance --------

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

    public List<Map<String,Object>> listQuizzesForTeacherAndClass(Connection c, long teacherId, long classId) throws Exception {
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

    public Quiz loadClassQuiz(Connection c, long quizId, long teacherId) throws Exception {
        String sql = """
          SELECT cq.id, cq.template_id, cq.status, cq.invite_code, cq.duration_seconds, cq.started_at, cq.ends_at, cq.ended_at,
                 qt.title
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id=cq.template_id
          WHERE cq.id=? AND cq.teacher_id=?
        """;
        Quiz quiz = null;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                quiz = new Quiz();
                quiz.quizId = rs.getLong("id");
                quiz.templateId = rs.getLong("template_id");
                quiz.status = rs.getString("status");
                quiz.inviteCode = rs.getString("invite_code");
                int dur = rs.getInt("duration_seconds");
                quiz.durationSeconds = rs.wasNull() ? null : dur;
                Timestamp st = rs.getTimestamp("started_at");
                Timestamp en = rs.getTimestamp("ends_at");
                Timestamp ed = rs.getTimestamp("ended_at");
                quiz.startedAt = st != null ? st.toLocalDateTime() : null;
                quiz.endsAt = en != null ? en.toLocalDateTime() : null;
                quiz.endedAt = ed != null ? ed.toLocalDateTime() : null;
                quiz.title = rs.getString("title");
            }
        }
        if (quiz == null) return null;

        // attach template questions/options
        Quiz tpl = loadTemplate(c, quiz.templateId);
        quiz.questions = tpl.questions;
        return quiz;
    }

    public Quiz loadClassQuizForStudentByCode(Connection c, String inviteCode) throws Exception {
        String sql = """
          SELECT cq.id, cq.template_id, cq.status, cq.invite_code, cq.duration_seconds, cq.started_at, cq.ends_at,
                 qt.title
          FROM class_quizzes cq
          JOIN quiz_templates qt ON qt.id=cq.template_id
          WHERE cq.invite_code=?
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, inviteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String status = rs.getString("status");
                if (!"RUNNING".equals(status)) return null;

                Quiz quiz = new Quiz();
                quiz.quizId = rs.getLong("id");
                quiz.templateId = rs.getLong("template_id");
                quiz.status = status;
                quiz.inviteCode = rs.getString("invite_code");
                quiz.durationSeconds = rs.getInt("duration_seconds");
                Timestamp st = rs.getTimestamp("started_at");
                Timestamp en = rs.getTimestamp("ends_at");
                quiz.startedAt = st != null ? st.toLocalDateTime() : null;
                quiz.endsAt = en != null ? en.toLocalDateTime() : null;
                quiz.title = rs.getString("title");

                Quiz tpl = loadTemplate(c, quiz.templateId);
                quiz.questions = tpl.questions;
                return quiz;
            }
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

    public void endQuizIfExpired(Connection c, long quizId) throws Exception {
        // idempotent: wenn ends_at in Vergangenheit und status RUNNING -> ENDED
        String sql = """
          UPDATE class_quizzes
          SET status='ENDED', ended_at=NOW()
          WHERE id=? AND status='RUNNING' AND ends_at IS NOT NULL AND ends_at <= NOW()
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            ps.executeUpdate();
        }
    }

    public void restartQuiz(Connection c, long quizId, long teacherId) throws Exception {
        // löscht alle Attempts & setzt Status zurück
        try (PreparedStatement ps1 = c.prepareStatement("DELETE aa FROM attempt_answers aa JOIN quiz_attempts qa ON qa.id=aa.attempt_id WHERE qa.quiz_id=?")) {
            ps1.setLong(1, quizId);
            ps1.executeUpdate();
        }
        try (PreparedStatement ps2 = c.prepareStatement("DELETE FROM quiz_attempts WHERE quiz_id=?")) {
            ps2.setLong(1, quizId);
            ps2.executeUpdate();
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

    // -------- Submit attempt --------

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

    // -------- Results / Feedback --------

    public List<QuizResultRow> computeResults(Connection c, long quizId, long teacherId) throws Exception {
        // Status ggf. auto beenden
        endQuizIfExpired(c, quizId);

        // Nur Lehrer darf sehen
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
                    r.questionId = rs.getLong("question_id");
                    r.pos = rs.getInt("pos");
                    r.questionText = rs.getString("question_text");
                    int correct = rs.getInt("correct_cnt");
                    int total = rs.getInt("total_cnt");
                    r.percentCorrect = total == 0 ? 0.0 : (100.0 * correct / total);
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    public List<String> listFeedback(Connection c, long quizId, long teacherId) throws Exception {
        endQuizIfExpired(c, quizId);

        try (PreparedStatement guard = c.prepareStatement("SELECT 1 FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            guard.setLong(1, quizId);
            guard.setLong(2, teacherId);
            try (ResultSet rs = guard.executeQuery()) {
                if (!rs.next()) return List.of();
            }
        }

        String sql = "SELECT feedback FROM quiz_attempts WHERE quiz_id=? AND feedback IS NOT NULL AND TRIM(feedback)<>'' ORDER BY submitted_at DESC";
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("feedback"));
            }
        }
        return out;
    }

    public LocalDateTime getEndedAt(Connection c, long quizId, long teacherId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT ended_at FROM class_quizzes WHERE id=? AND teacher_id=?")) {
            ps.setLong(1, quizId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp t = rs.getTimestamp("ended_at");
                return t == null ? null : t.toLocalDateTime();
            }
        }
    }

    public LocalDateTime getEndsAt(Connection c, long quizId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT ends_at FROM class_quizzes WHERE id=?")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp t = rs.getTimestamp("ends_at");
                return t == null ? null : t.toLocalDateTime();
            }
        }
    }

    public String getStatus(Connection c, long quizId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT status FROM class_quizzes WHERE id=?")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("status");
            }
        }
    }
}
