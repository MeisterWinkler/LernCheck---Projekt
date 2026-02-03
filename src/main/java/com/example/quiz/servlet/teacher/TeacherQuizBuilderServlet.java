package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.dao.QuizDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherQuizBuilderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        try (Connection c = DB.getConnection(getServletContext())) {
            ClassDao classDao = new ClassDao();
            req.setAttribute("classes", classDao.listForTeacher(c, teacherId));
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/quiz_builder.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        try (Connection c = DB.getConnection(getServletContext())) {
            // Dein bestehender POST-Flow bleibt (Template anlegen, Fragen speichern, Quiz zuweisen etc.)
            // => hier NICHT neu erfinden, da du den schon hast.
            // Wichtig: wenn du hier irgendwo listAll/findById nutzt -> auch auf teacherId umbauen.

            // Wenn du willst, kann ich dir deinen aktuellen doPost auch 1:1 umschreiben,
            // aber dafür müsste ich ihn sehen. (Du wolltest keine Rückfragen – daher lasse ich deinen Flow unberührt.)
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}