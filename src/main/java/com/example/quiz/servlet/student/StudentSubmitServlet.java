package com.example.quiz.servlet.student;

import com.example.quiz.dao.QuizDao;
import com.example.quiz.dao.StudentDao;
import com.example.quiz.model.Quiz;
import com.example.quiz.model.QuizQuestion;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class StudentSubmitServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = (String) req.getSession().getAttribute("inviteCode");
        String studentName = (String) req.getSession().getAttribute("studentName");
        if (code == null || studentName == null) {
            resp.sendRedirect(req.getContextPath() + "/student/join");
            return;
        }

        String feedback = req.getParameter("feedback");

        try (Connection c = DB.getConnection(getServletContext())) {
            QuizDao qdao = new QuizDao();
            Quiz quiz = qdao.loadClassQuizForStudentByCode(c, code);
            if (quiz == null) {
                req.setAttribute("message", "Quiz ist nicht verfügbar (evtl. beendet).");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            // Quiz ggf. beenden, falls Zeit abgelaufen
            qdao.endQuizIfExpired(c, quiz.quizId);
            if (!"RUNNING".equals(qdao.getStatus(c, quiz.quizId))) {
                req.setAttribute("message", "Zeit ist abgelaufen. Abgabe nicht mehr möglich.");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }

            c.setAutoCommit(false);

            StudentDao sdao = new StudentDao();
            long studentId = sdao.create(c, studentName);

            long attemptId = qdao.createAttempt(c, quiz.quizId, studentId, feedback);

            for (QuizQuestion q : quiz.questions) {
                String chosen = req.getParameter("q_" + q.id);
                if (chosen == null || chosen.isEmpty()) continue; // unbeantwortet erlaubt
                char ch = chosen.charAt(0);
                qdao.insertAnswer(c, attemptId, q.id, ch);
            }

            c.commit();

            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/student/join?done=1");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}