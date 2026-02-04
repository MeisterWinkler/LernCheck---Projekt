package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.dao.QuizDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherTemplateSelectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            ClassDao classDao = new ClassDao();

            // ✅ nur Templates des Lehrers
            req.setAttribute("templates", qdao.listTemplates(c, teacherId));
            // ✅ nur Klassen des Lehrers
            req.setAttribute("classes", classDao.listForTeacher(c, teacherId));

            req.getRequestDispatcher("/WEB-INF/jsp/teacher/template_select.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        String templateIdStr = req.getParameter("templateId");
        String classIdStr = req.getParameter("classId");

        long templateId;
        long classId;

        try {
            templateId = Long.parseLong(templateIdStr);
            classId = Long.parseLong(classIdStr);
        } catch (Exception e) {
            req.setAttribute("error", "Ungültige Auswahl.");
            doGet(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {
            ClassDao classDao = new ClassDao();

            // ✅ Sicherheit: Lehrer darf nur in seine Klasse hochladen
            if (classDao.findByIdForTeacher(c, classId, teacherId) == null) {
                req.setAttribute("error", "Diese Klasse gehört dir nicht.");
                doGet(req, resp);
                return;
            }

            QuizDao qdao = new QuizDao();

            // ✅ EXISTIERT bei dir (nutzt du auch im QuizBuilder)
            qdao.createClassQuizFromTemplate(c, teacherId, classId, templateId);

            // ✅ danach sofort zur Klasse, damit du das Quiz siehst
            resp.sendRedirect(req.getContextPath() + "/teacher/class?id=" + classId);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}