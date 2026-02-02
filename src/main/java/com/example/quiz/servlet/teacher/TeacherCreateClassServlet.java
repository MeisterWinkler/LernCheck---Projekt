package com.example.quiz.servlet.teacher;

import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TeacherCreateClassServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/WEB-INF/jsp/teacher/create_class.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = req.getParameter("name");

        if (name == null || name.isBlank()) {
            req.setAttribute("error", "Bitte Klassennamen eingeben.");
            doGet(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {

            // doppelte Klassen verhindern
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id FROM classes WHERE name=?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    req.setAttribute("error", "Diese Klasse existiert bereits.");
                    doGet(req, resp);
                    return;
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO classes(name) VALUES (?)")) {
                ps.setString(1, name.trim());
                ps.executeUpdate();
            }

            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}