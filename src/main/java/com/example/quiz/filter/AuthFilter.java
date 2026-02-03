package com.example.quiz.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        // statics immer erlauben
        if (uri.startsWith(ctx + "/static/")) {
            chain.doFilter(request, response);
            return;
        }

        // Login/Register immer erlauben
        if (uri.equals(ctx + "/teacher/login") || uri.equals(ctx + "/teacher/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Studentbereich nicht vom Teacher-Filter blocken
        if (uri.startsWith(ctx + "/student/")) {
            chain.doFilter(request, response);
            return;
        }

        // Teacherbereich schützen
        if (uri.startsWith(ctx + "/teacher/")) {
            HttpSession session = req.getSession(false);
            Object teacherId = (session == null) ? null : session.getAttribute("teacherId");

            if (teacherId == null) {
                resp.sendRedirect(ctx + "/teacher/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}