package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.QuizResultRow;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class TeacherQuizResultsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        long teacherId = (long) session.getAttribute("teacherId");

        long quizId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();

            // ✅ sorgt dafür, dass abgelaufene RUNNING Quizze beendet werden
            dao.endAllExpired(c);

            List<QuizResultRow> rows = dao.computeResults(c, quizId, teacherId);
            LocalDateTime endedAt = dao.getEndedAt(c, quizId, teacherId);

            // ✅ Feedback für dieses Quiz
            List<String> feedback = dao.listFeedback(c, quizId, teacherId);

            req.setAttribute("rows", rows);
            req.setAttribute("quizId", quizId);
            req.setAttribute("endedAt", endedAt);
            req.setAttribute("feedback", feedback);

            req.getRequestDispatcher("/WEB-INF/jsp/teacher/results.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}