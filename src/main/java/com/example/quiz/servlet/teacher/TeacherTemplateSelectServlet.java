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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            ClassDao classDao = new ClassDao();
            req.setAttribute("templates", qdao.listTemplates(c, teacherId));
            req.setAttribute("classes", classDao.listAll(c));
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/template_select.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");
        long templateId = Long.parseLong(req.getParameter("templateId"));
        long classId = Long.parseLong(req.getParameter("classId"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            long classQuizId = qdao.createClassQuizFromTemplate(c, teacherId, classId, templateId);
            resp.sendRedirect(req.getContextPath() + "/teacher/class?id=" + classId + "&createdQuizId=" + classQuizId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}