package com.example.quiz.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        boolean isStatic = uri.startsWith(ctx + "/static/");
        boolean isTeacherLogin = uri.equals(ctx + "/teacher/login");
        boolean isTeacherRegister = uri.equals(ctx + "/teacher/register");

        // Student-Bereich nie durch Teacher-Auth blockieren
        boolean isStudent = uri.startsWith(ctx + "/student/");

        if (isStatic || isStudent || isTeacherLogin || isTeacherRegister) {
            chain.doFilter(request, response);
            return;
        }

        // Teacher-Bereich schützen
        if (uri.startsWith(ctx + "/teacher/")) {
            HttpSession session = req.getSession(false); // ✅ wichtig
            Object teacherId = (session == null) ? null : session.getAttribute("teacherId");

            if (teacherId == null) {
                resp.sendRedirect(ctx + "/teacher/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}