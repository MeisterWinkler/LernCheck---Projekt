package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class TeacherQuizFeedbackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            List<String> feedback = qdao.listFeedback(c, quizId, teacherId);
            req.setAttribute("feedback", feedback);
            req.setAttribute("quizId", quizId);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/feedback.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}