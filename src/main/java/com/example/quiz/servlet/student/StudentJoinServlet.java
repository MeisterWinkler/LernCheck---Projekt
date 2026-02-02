package com.example.quiz.servlet.student;

import com.example.quiz.util.DB;
import com.example.quiz.dao.QuizDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class StudentJoinServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/student/join.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            req.setAttribute("error", "Bitte Einladungscode und Namen eingeben.");
            req.getRequestDispatcher("/WEB-INF/jsp/student/join.jsp").forward(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            var quiz = qdao.loadClassQuizForStudentByCode(c, code.trim().toUpperCase());
            if (quiz == null) {
                req.setAttribute("error", "Ungültiger Code oder Quiz nicht gestartet.");
                req.getRequestDispatcher("/WEB-INF/jsp/student/join.jsp").forward(req, resp);
                return;
            }

            HttpSession s = req.getSession(true);
            s.setAttribute("studentName", name.trim());
            s.setAttribute("inviteCode", code.trim().toUpperCase());

            resp.sendRedirect(req.getContextPath() + "/student/quiz");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}