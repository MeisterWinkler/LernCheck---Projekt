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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");
        long classId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            qdao.endAllExpired(c);

            ClassDao classDao = new ClassDao();
            Klass k = classDao.findByIdForTeacher(c, classId, teacherId);

            if (k == null) {
                req.setAttribute("message", "Klasse nicht gefunden oder gehört dir nicht.");
                req.setAttribute("showTeacherLinks", true);
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("klass", k);
            req.setAttribute("quizzes", qdao.listQuizzesForTeacherAndClass(c, teacherId, classId));

            req.getRequestDispatcher("/WEB-INF/jsp/teacher/class_quizzes.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}