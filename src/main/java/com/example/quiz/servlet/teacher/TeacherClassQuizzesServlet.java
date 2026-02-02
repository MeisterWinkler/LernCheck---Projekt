package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Klass;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class TeacherClassQuizzesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String classIdStr = req.getParameter("id");
        if (classIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/teacher/dashboard");
            return;
        }
        long classId = Long.parseLong(classIdStr);
        long teacherId = (long) req.getSession().getAttribute("teacherId");

        try (Connection c = DB.getConnection(getServletContext())) {
            ClassDao classDao = new ClassDao();
            Klass k = classDao.findById(c, classId);
            if (k == null) {
                req.setAttribute("message", "Klasse nicht gefunden.");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            QuizDao qdao = new QuizDao();
            req.setAttribute("klass", k);
            req.setAttribute("quizzes", qdao.listQuizzesForTeacherAndClass(c, teacherId, classId));
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/class_quizzes.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}