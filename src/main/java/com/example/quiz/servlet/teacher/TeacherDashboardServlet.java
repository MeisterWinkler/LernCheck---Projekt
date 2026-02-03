package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.dao.QuizDao;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");

        try (Connection c = DB.getConnection(getServletContext())) {
            new QuizDao().endAllExpired(c);

            ClassDao classDao = new ClassDao();
            req.setAttribute("classes", classDao.listForTeacher(c, teacherId));

            req.getRequestDispatcher("/WEB-INF/jsp/teacher/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
