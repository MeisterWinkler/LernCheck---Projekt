package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import com.example.quiz.util.Token;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherStartQuizServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            Quiz quiz = qdao.loadClassQuiz(c, quizId, teacherId);
            if (quiz == null) {
                req.setAttribute("message", "Quiz nicht gefunden.");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("quiz", quiz);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/start_quiz.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        int durationMin = Integer.parseInt(req.getParameter("durationMin"));
        if (durationMin <= 0) durationMin = 10;
        int durationSeconds = durationMin * 60;

        String invite = Token.inviteCode(6);

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            qdao.startQuiz(c, quizId, teacherId, durationSeconds, invite);
            resp.sendRedirect(req.getContextPath() + "/teacher/quiz/start?id=" + quizId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}