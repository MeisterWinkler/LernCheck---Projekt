package com.example.quiz.servlet.teacher;

import com.example.quiz.dao.ClassDao;
import com.example.quiz.dao.QuizDao;
import com.example.quiz.model.Klass;
import com.example.quiz.util.DB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class TeacherQuizBuilderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection c = DB.getConnection(getServletContext())) {
            ClassDao classDao = new ClassDao();
            List<Klass> classes = classDao.listAll(c);
            req.setAttribute("classes", classes);
            req.getRequestDispatcher("/WEB-INF/jsp/teacher/quiz_builder.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long teacherId = (long) req.getSession().getAttribute("teacherId");

        String title = req.getParameter("title"); // Thema
        String classIdStr = req.getParameter("classId"); // ausgewählte Klasse
        if (title == null || title.trim().isEmpty() || classIdStr == null) {
            req.setAttribute("error", "Bitte Thema und Klasse wählen.");
            doGet(req, resp);
            return;
        }
        long classId = Long.parseLong(classIdStr);

        // Fragen kommen als arrays: qText[], aText[], bText[], cText[], dText[], correct[]
        String[] qText = req.getParameterValues("qText");
        String[] aText = req.getParameterValues("aText");
        String[] bText = req.getParameterValues("bText");
        String[] cText = req.getParameterValues("cText");
        String[] dText = req.getParameterValues("dText");
        String[] correct = req.getParameterValues("correct"); // "A"/"B"/"C"/"D"

        if (qText == null || qText.length == 0) {
            req.setAttribute("error", "Bitte mindestens 1 Frage anlegen.");
            doGet(req, resp);
            return;
        }

        try (Connection c = DB.getConnection(getServletContext())) {
            c.setAutoCommit(false);
            QuizDao qdao = new QuizDao();

            long templateId = qdao.createTemplate(c, teacherId, title.trim());

            for (int i = 0; i < qText.length; i++) {
                String qt = safeArr(qText, i);
                if (qt == null || qt.trim().isEmpty()) continue;

                char corr = safeArr(correct, i) != null ? safeArr(correct, i).charAt(0) : 'A';
                long qId = qdao.createTemplateQuestion(c, templateId, qt.trim(), corr, i + 1);

                qdao.upsertOption(c, qId, 'A', safeArr(aText, i));
                qdao.upsertOption(c, qId, 'B', safeArr(bText, i));
                qdao.upsertOption(c, qId, 'C', safeArr(cText, i));
                qdao.upsertOption(c, qId, 'D', safeArr(dText, i));
            }

            long classQuizId = qdao.createClassQuizFromTemplate(c, teacherId, classId, templateId);

            c.commit();

            resp.sendRedirect(req.getContextPath() + "/teacher/class?id=" + classId + "&createdQuizId=" + classQuizId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String safeArr(String[] a, int i) {
        if (a == null || i < 0 || i >= a.length) return "";
        return a[i] == null ? "" : a[i];
    }
}