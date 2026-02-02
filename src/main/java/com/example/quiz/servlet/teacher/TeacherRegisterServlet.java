package com.example.quiz.servlet.teacher;

import com.example.quiz.util.DB;
import com.example.quiz.util.Passwords;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TeacherRegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/WEB-INF/jsp/teacher/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {

            req.setAttribute("error", "Bitte Benutzername und Passwort ausfüllen.");
            doGet(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {

            // prüfen ob Benutzer existiert
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id FROM teachers WHERE username=?")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    req.setAttribute("error", "Benutzername existiert bereits.");
                    doGet(req, resp);
                    return;
                }
            }

            String hash = Passwords.hash(password);

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO teachers(username,password_hash) VALUES (?,?)")) {

                ps.setString(1, username);
                ps.setString(2, hash);
                ps.executeUpdate();
            }

            resp.sendRedirect(req.getContextPath() + "/teacher/login?registered=1");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}