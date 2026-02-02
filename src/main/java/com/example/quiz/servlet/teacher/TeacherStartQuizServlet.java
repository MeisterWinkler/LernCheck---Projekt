package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;

public class TeacherStartQuizServlet extends HttpServlet {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RND = new SecureRandom();

    private static String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
            return;
        }

        long quizId = Long.parseLong(idStr);

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            Quiz quiz = dao.loadClassQuiz(c, quizId, teacherId);
            if (quiz == null) {
                resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
                return;
            }

            req.setAttribute("quiz", quiz);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/start_quiz.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        String idStr = req.getParameter("id");
        String durStr = req.getParameter("durationMinutes");

        if (idStr == null || durStr == null) {
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
            return;
        }

        long quizId = Long.parseLong(idStr);

        int minutes;
        try {
            minutes = Integer.parseInt(durStr);
        } catch (NumberFormatException nfe) {
            minutes = -1;
        }

        if (minutes < 1 || minutes > 180) {
            try (Connection c = DB.getConnection(getServletContext())) {
                QuizDao dao = new QuizDao();
                Quiz quiz = dao.loadClassQuiz(c, quizId, teacherId);
                req.setAttribute("quiz", quiz);
                req.setAttribute("error", "Bitte eine Dauer zwischen 1 und 180 Minuten wählen.");
                req.getRequestDispatcher("/WEB-INF/jsp/teacher/start_quiz.jsp").forward(req, resp);
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        int durationSeconds = minutes * 60;
        String inviteCode = randomCode(6);

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            dao.startQuiz(c, quizId, teacherId, durationSeconds, inviteCode);
            resp.sendRedirect(req.getContextPath() + "/teacher/quiz/start?id=" + quizId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}