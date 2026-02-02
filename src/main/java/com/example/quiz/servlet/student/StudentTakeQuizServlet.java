package com.example.quiz.servlet.student;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;

public class StudentTakeQuizServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = (String) req.getSession().getAttribute("inviteCode");
        if (code == null) {
            resp.sendRedirect(req.getContextPath() + "/student/join");
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            Quiz quiz = qdao.loadClassQuizForStudentByCode(c, code);
            if (quiz == null) {
                req.setAttribute("message", "Quiz ist nicht verfügbar (evtl. beendet).");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            // serverseitig ablaufen lassen
            LocalDateTime endsAt = qdao.getEndsAt(c, quiz.quizId);
            req.setAttribute("quiz", quiz);
            req.setAttribute("endsAt", endsAt); // für JS Countdown
            req.getRequestDispatcher("/WEB-INF/jsp/student/take_quiz.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}