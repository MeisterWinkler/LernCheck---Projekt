package com.example.quiz.servlet.student;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.ZoneId;

public class StudentTakeQuizServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("inviteCode") == null) {
            resp.sendRedirect(req.getContextPath() + "/student/join");
            return;
        }

        String code = (String) s.getAttribute("inviteCode");

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            Quiz quiz = dao.loadClassQuizForStudentByCode(c, code);

            if (quiz == null) {
                req.setAttribute("message", "Quiz ist nicht aktiv oder bereits abgelaufen.");
                req.setAttribute("showStudentOnly", true);
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            // für JS-Timer
            if (quiz.getEndsAt() != null) {
                long endsAtMillis = quiz.getEndsAt()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
                req.setAttribute("endsAtMillis", endsAtMillis);
            }

            req.setAttribute("quiz", quiz);
            req.getRequestDispatcher("/WEB-INF/jsp/student/take_quiz.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}