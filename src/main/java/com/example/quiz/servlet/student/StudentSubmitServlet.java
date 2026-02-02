package com.example.quiz.servlet.student;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;

public class StudentSubmitServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("studentId") == null || s.getAttribute("inviteCode") == null) {
            req.setAttribute("message", "Bitte zuerst über den Einladungscode beitreten.");
            req.setAttribute("showStudentOnly", true);
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
            return;
        }

        long studentId = (long) s.getAttribute("studentId");
        String code = (String) s.getAttribute("inviteCode");

        String feedback = req.getParameter("feedback"); // optional
        if (feedback != null && feedback.isBlank()) feedback = null;

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao quizDao = new QuizDao();

            // Quiz laden (dabei wird ggf. ENDED gesetzt, wenn abgelaufen)
            Quiz quiz = quizDao.loadClassQuizForStudentByCode(c, code);

            if (quiz == null) {
                req.setAttribute("message", "Zeit ist abgelaufen. Abgabe nicht mehr möglich.");
                req.setAttribute("showStudentOnly", true);
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            // zusätzliche Sicherheit: endsAt prüfen
            if (quiz.getEndsAt() != null && quiz.getEndsAt().isBefore(LocalDateTime.now())) {
                req.setAttribute("message", "Zeit ist abgelaufen. Abgabe nicht mehr möglich.");
                req.setAttribute("showStudentOnly", true);
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            long attemptId = quizDao.createAttempt(c, quiz.getQuizId(), studentId, feedback);

            for (var q : quiz.getQuestions()) {
                String p = req.getParameter("answer_" + q.getId());
                if (p != null && !p.isBlank()) {
                    char chosen = p.charAt(0);
                    quizDao.insertAnswer(c, attemptId, q.getId(), chosen);
                }
            }

            // Session sauber machen
            s.invalidate();

            resp.sendRedirect(req.getContextPath() + "/student/join?done=1");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}