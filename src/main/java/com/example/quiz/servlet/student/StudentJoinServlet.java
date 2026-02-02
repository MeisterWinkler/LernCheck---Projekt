package com.example.quiz.servlet.student;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.dao.StudentDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

public class StudentJoinServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/student/join.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = req.getParameter("code");
        if (code == null || code.isBlank()) {
            req.setAttribute("error", "Bitte Einladungscode eingeben.");
            doGet(req, resp);
            return;
        }

        code = code.trim().toUpperCase();

        try (Connection c = DB.getConnection(req.getServletContext())) {
            QuizDao quizDao = new QuizDao();
            Quiz quiz = quizDao.loadClassQuizForStudentByCode(c, code);

            if (quiz == null) {
                req.setAttribute("error", "Ungültiger Code oder Quiz ist nicht aktiv (evtl. abgelaufen).");
                doGet(req, resp);
                return;
            }

            StudentDao studentDao = new StudentDao();
            String anonName = "Student-" + UUID.randomUUID().toString().substring(0, 8);
            long studentId = studentDao.create(c, anonName);

            HttpSession s = req.getSession(true);
            s.setAttribute("studentId", studentId);
            s.setAttribute("quizId", quiz.getQuizId());
            s.setAttribute("inviteCode", code);

            resp.sendRedirect(req.getContextPath() + "/student/quiz");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
