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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        long teacherId = (long) s.getAttribute("teacherId");

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
            return;
        }

        long quizId = Long.parseLong(idStr);

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            List<String> feedback = dao.listFeedback(c, quizId, teacherId);

            req.setAttribute("feedback", feedback);
            req.setAttribute("quizId", quizId);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/feedback.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}