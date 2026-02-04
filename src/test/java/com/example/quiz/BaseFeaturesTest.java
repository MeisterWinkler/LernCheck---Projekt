package com.example.quiz;

import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BaseFeaturesTest {

    private Connection c;

    private static String dbUrl()  { return System.getProperty("test.db.url",  "jdbc:mariadb://localhost:3306/quizapp"); }
    private static String dbUser() { return System.getProperty("test.db.user", "root"); }
    private static String dbPass() { return System.getProperty("test.db.pass", ""); }

    @BeforeEach
    void openTx() throws Exception {
        c = DriverManager.getConnection(dbUrl(), dbUser(), dbPass());
        c.setAutoCommit(false); // <<< wichtig: jeder Test rollbackt
    }

    @AfterEach
    void rollbackAndClose() throws Exception {
        if (c != null) {
            c.rollback();
            c.close();
        }
    }

    @Test
    void teacher_can_create_class_and_only_sees_own_classes() throws Exception {
        long t1 = insertTeacher("t_test1", "pw1");
        long t2 = insertTeacher("t_test2", "pw2");

        long c1 = insertClass(t1, "fia-test-1");
        long c2 = insertClass(t2, "fia-test-2");

        List<Long> t1Classes = selectIds("SELECT id FROM classes WHERE teacher_id=? ORDER BY id", t1);
        List<Long> t2Classes = selectIds("SELECT id FROM classes WHERE teacher_id=? ORDER BY id", t2);

        assertTrue(t1Classes.contains(c1));
        assertFalse(t1Classes.contains(c2));

        assertTrue(t2Classes.contains(c2));
        assertFalse(t2Classes.contains(c1));
    }

    @Test
    void template_create_add_questions_and_options() throws Exception {
        long teacherId = insertTeacher("t_tpl", "pw");

        long templateId = insertTemplate(teacherId, "Subnetting Test");

        long q1 = insertTemplateQuestion(templateId, "Was ist 1+1?", 'D', 1);
        upsertTemplateOption(q1, 'A', "4");
        upsertTemplateOption(q1, 'B', "6");
        upsertTemplateOption(q1, 'C', "3");
        upsertTemplateOption(q1, 'D', "2");

        long countQ = scalarLong("SELECT COUNT(*) FROM template_questions WHERE template_id=?", templateId);
        long countO = scalarLong("SELECT COUNT(*) FROM template_options WHERE question_id=?", q1);

        assertEquals(1, countQ);
        assertEquals(4, countO);
    }

    @Test
    void create_class_quiz_start_join_submit_results_and_weak_questions() throws Exception {
        long teacherId = insertTeacher("t_flow", "pw");
        long classId = insertClass(teacherId, "fia-flow");

        // Template + 2 Fragen
        long templateId = insertTemplate(teacherId, "Flow Quiz");

        long q1 = insertTemplateQuestion(templateId, "Q1", 'B', 1);
        upsertTemplateOption(q1, 'A', "A1");
        upsertTemplateOption(q1, 'B', "B1");
        upsertTemplateOption(q1, 'C', "C1");
        upsertTemplateOption(q1, 'D', "D1");

        long q2 = insertTemplateQuestion(templateId, "Q2", 'D', 2);
        upsertTemplateOption(q2, 'A', "A2");
        upsertTemplateOption(q2, 'B', "B2");
        upsertTemplateOption(q2, 'C', "C2");
        upsertTemplateOption(q2, 'D', "D2");

        // Class Quiz erstellen
        long quizId = insertClassQuiz(teacherId, classId, templateId);

        // Quiz starten
        String code = "ZZ" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        int durationSeconds = 120;
        startQuiz(quizId, code, durationSeconds);

        // Assert running + invite code gesetzt
        String status = scalarString("SELECT status FROM class_quizzes WHERE id=?", quizId);
        String invite = scalarString("SELECT invite_code FROM class_quizzes WHERE id=?", quizId);
        assertEquals("RUNNING", status);
        assertEquals(code, invite);

        // Student + Attempt + Answers
        long studentId = insertStudent("Student-X");

        long attemptId = insertAttempt(quizId, studentId, "Feedback-123");

        // Antworten: absichtlich 1 richtig, 1 falsch -> je Frage 0% oder 100% je nach Versuch
        // Wir machen 2 Attempts, damit wir Prozent berechnen können (<=50% triggers weak)
        insertAttemptAnswer(attemptId, q1, 'B'); // richtig
        insertAttemptAnswer(attemptId, q2, 'A'); // falsch

        long studentId2 = insertStudent("Student-Y");
        long attemptId2 = insertAttempt(quizId, studentId2, "");
        insertAttemptAnswer(attemptId2, q1, 'A'); // falsch
        insertAttemptAnswer(attemptId2, q2, 'A'); // falsch

        // Prozent richtig je Frage berechnen (auf Basis deiner Tabellen)
        Map<Long, Double> pct = percentCorrectPerQuestion(quizId);

        // q1: 1/2 richtig = 50%
        // q2: 0/2 richtig = 0%
        assertEquals(50.0, pct.get(q1), 0.0001);
        assertEquals(0.0, pct.get(q2), 0.0001);

        // Weak questions speichern wenn <= 50%
        // (das ist eure Kernlogik aus dem Feature)
        upsertWeakQuestion(teacherId, quizId, q1, pct.get(q1));
        upsertWeakQuestion(teacherId, quizId, q2, pct.get(q2));

        long weakCount = scalarLong("SELECT COUNT(*) FROM weak_questions WHERE teacher_id=? AND quiz_id=?", teacherId, quizId);
        assertEquals(2, weakCount);
    }

    // -------------------------
    // Helpers (SQL passend zu deinem Schema)
    // -------------------------

    private long insertTeacher(String username, String rawPassword) throws Exception {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO teachers(username,password_hash,created_at) VALUES(?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private long insertClass(long teacherId, String name) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO classes(name,teacher_id) VALUES(?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, name);
            ps.setLong(2, teacherId);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private long insertTemplate(long teacherId, String title) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO quiz_templates(teacher_id,title,created_at) VALUES(?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setLong(1, teacherId);
            ps.setString(2, title);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private long insertTemplateQuestion(long templateId, String text, char correct, int pos) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO template_questions(template_id,question_text,correct_option,pos) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setLong(1, templateId);
            ps.setString(2, text);
            ps.setString(3, String.valueOf(correct));
            ps.setInt(4, pos);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private void upsertTemplateOption(long questionId, char letter, String text) throws Exception {
        // Dein schema hat template_options(question_id, option_letter, option_text)
        // Wir machen: delete+insert (simpel und stabil für Tests)
        try (PreparedStatement del = c.prepareStatement(
                "DELETE FROM template_options WHERE question_id=? AND option_letter=?"
        )) {
            del.setLong(1, questionId);
            del.setString(2, String.valueOf(letter));
            del.executeUpdate();
        }
        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO template_options(question_id, option_letter, option_text) VALUES(?,?,?)"
        )) {
            ins.setLong(1, questionId);
            ins.setString(2, String.valueOf(letter));
            ins.setString(3, text);
            ins.executeUpdate();
        }
    }

    private long insertClassQuiz(long teacherId, long classId, long templateId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO class_quizzes(teacher_id,class_id,template_id,created_at,status) VALUES(?,?,?,?, 'NOT_STARTED')",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setLong(1, teacherId);
            ps.setLong(2, classId);
            ps.setLong(3, templateId);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private void startQuiz(long quizId, String code, int durationSeconds) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE class_quizzes " +
                        "SET status='RUNNING', invite_code=?, duration_seconds=?, started_at=NOW(), ends_at=DATE_ADD(NOW(), INTERVAL ? SECOND), ended_at=NULL " +
                        "WHERE id=?"
        )) {
            ps.setString(1, code);
            ps.setInt(2, durationSeconds);
            ps.setInt(3, durationSeconds);
            ps.setLong(4, quizId);
            ps.executeUpdate();
        }
    }

    private long insertStudent(String displayName) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO students(display_name) VALUES(?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, displayName);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private long insertAttempt(long quizId, long studentId, String feedback) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO quiz_attempts(quiz_id,student_id,submitted_at,feedback) VALUES(?,?,NOW(),?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setLong(1, quizId);
            ps.setLong(2, studentId);
            ps.setString(3, feedback);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private void insertAttemptAnswer(long attemptId, long questionId, char chosen) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO attempt_answers(attempt_id,question_id,chosen_option) VALUES(?,?,?)"
        )) {
            ps.setLong(1, attemptId);
            ps.setLong(2, questionId);
            ps.setString(3, String.valueOf(chosen));
            ps.executeUpdate();
        }
    }

    private Map<Long, Double> percentCorrectPerQuestion(long quizId) throws Exception {
        // join: class_quizzes -> template_questions
        long templateId = scalarLong("SELECT template_id FROM class_quizzes WHERE id=?", quizId);

        // Alle Fragen des Templates
        List<Long> qIds = selectIds("SELECT id FROM template_questions WHERE template_id=? ORDER BY pos", templateId);

        Map<Long, Double> result = new HashMap<>();
        for (long qId : qIds) {
            String correct = scalarString("SELECT correct_option FROM template_questions WHERE id=?", qId);

            long total = scalarLong(
                    "SELECT COUNT(*) " +
                            "FROM attempt_answers aa " +
                            "JOIN quiz_attempts qa ON qa.id = aa.attempt_id " +
                            "WHERE qa.quiz_id=? AND aa.question_id=?",
                    quizId, qId
            );

            long correctCnt = scalarLong(
                    "SELECT COUNT(*) " +
                            "FROM attempt_answers aa " +
                            "JOIN quiz_attempts qa ON qa.id = aa.attempt_id " +
                            "WHERE qa.quiz_id=? AND aa.question_id=? AND aa.chosen_option=?",
                    quizId, qId, correct
            );

            double pct = total == 0 ? 0.0 : (correctCnt * 100.0 / total);
            // runden auf 2 Dezimalstellen (wie UI Wunsch)
            pct = Math.round(pct * 100.0) / 100.0;
            result.put(qId, pct);
        }
        return result;
    }

    private void upsertWeakQuestion(long teacherId, long quizId, long questionId, double percentCorrect) throws Exception {
        // Für Tests: einfach insert (wenn ihr unique constraints habt: vorher delete)
        try (PreparedStatement del = c.prepareStatement(
                "DELETE FROM weak_questions WHERE teacher_id=? AND quiz_id=? AND question_id=?"
        )) {
            del.setLong(1, teacherId);
            del.setLong(2, quizId);
            del.setLong(3, questionId);
            del.executeUpdate();
        }

        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO weak_questions(teacher_id,quiz_id,question_id,percent_correct,created_at) VALUES(?,?,?,?,NOW())"
        )) {
            ins.setLong(1, teacherId);
            ins.setLong(2, quizId);
            ins.setLong(3, questionId);
            ins.setDouble(4, percentCorrect);
            ins.executeUpdate();
        }
    }

    // --------- small generic helpers

    private long generatedId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            assertTrue(rs.next(), "No generated key returned");
            return rs.getLong(1);
        }
    }

    private long scalarLong(String sql, Object... args) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "No result for scalarLong");
                return rs.getLong(1);
            }
        }
    }

    private String scalarString(String sql, Object... args) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "No result for scalarString");
                return rs.getString(1);
            }
        }
    }

    private List<Long> selectIds(String sql, Object... args) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> out = new ArrayList<>();
                while (rs.next()) out.add(rs.getLong(1));
                return out;
            }
        }
    }

    private void bind(PreparedStatement ps, Object... args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof Long l) ps.setLong(i + 1, l);
            else if (a instanceof Integer n) ps.setInt(i + 1, n);
            else if (a instanceof String s) ps.setString(i + 1, s);
            else if (a instanceof Double d) ps.setDouble(i + 1, d);
            else ps.setObject(i + 1, a);
        }
    }
}