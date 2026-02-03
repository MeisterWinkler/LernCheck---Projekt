package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherCreateClassServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/teacher/create_class.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");
        String name = req.getParameter("name");

        if (name == null || name.isBlank()) {
            req.setAttribute("error", "Bitte Klassenname eingeben.");
            doGet(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {
            new ClassDao().create(c, teacherId, name.trim());
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}