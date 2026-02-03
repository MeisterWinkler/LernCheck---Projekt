package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.TeacherDao;
import com.example.quiz.model.Teacher;
import com.example.quiz.util.DB;
import com.example.quiz.util.Passwords;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ✅ Login JSP ist NICHT direkt aufrufbar (liegt unter /WEB-INF)
        req.getRequestDispatcher("/WEB-INF/jsp/teacher/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null) username = "";
        if (password == null) password = "";

        try (Connection c = DB.getConnection(getServletContext())) {

            TeacherDao dao = new TeacherDao();
            Teacher t = dao.findByUsername(c, username.trim());

            if (t == null || !Passwords.verify(password, t.getPasswordHash())) {
                req.setAttribute("error", "Benutzername oder Passwort ist falsch.");
                req.getRequestDispatcher("/WEB-INF/jsp/teacher/login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("teacherId", t.getId());

            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}