package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherRestartQuizServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            qdao.restartQuiz(c, quizId, teacherId);
            resp.sendRedirect(req.getContextPath() + "/teacher/quiz/start?id=" + quizId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}