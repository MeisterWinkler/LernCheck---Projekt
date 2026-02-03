package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.util.DB;
import com.example.quiz.util.Token;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class TeacherStartQuizServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            dao.endAllExpired(c);

            Quiz quiz = dao.loadClassQuiz(c, quizId, teacherId);
            if (quiz == null) {
                req.setAttribute("message", "Quiz nicht gefunden.");
                req.setAttribute("showTeacherLinks", true);
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            List<QuizDao.WeakSourceQuiz> sources = dao.listLast3WeakSourceQuizzes(c, teacherId, quizId);

            req.setAttribute("quiz", quiz);
            req.setAttribute("weakSources", sources);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/start_quiz.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long teacherId = (long) req.getSession(false).getAttribute("teacherId");
        long quizId = Long.parseLong(req.getParameter("id"));

        int durationMinutes;
        try {
            durationMinutes = Integer.parseInt(req.getParameter("durationMinutes"));
        } catch (Exception ex) {
            durationMinutes = 10;
        }
        if (durationMinutes < 1) durationMinutes = 1;

        String weakSourceIdStr = req.getParameter("weakSourceQuizId");

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao dao = new QuizDao();
            dao.endAllExpired(c);

            if (weakSourceIdStr != null && !weakSourceIdStr.isBlank()) {
                long sourceQuizId = Long.parseLong(weakSourceIdStr);
                if (sourceQuizId > 0) {
                    dao.copyWeakQuestionsToQuiz(c, teacherId, quizId, sourceQuizId);
                }
            }

            String code = Token.generate(6);
            dao.startQuiz(c, quizId, teacherId, durationMinutes * 60, code);

            resp.sendRedirect(req.getContextPath() + "/teacher/quiz/start?id=" + quizId);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}