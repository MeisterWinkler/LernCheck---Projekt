package com.example.quiz.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

        // Lehrer darf ohne Login auf Login/Register/Static zugreifen
        boolean isTeacherLogin = uri.equals(ctx + "/teacher/login");
        boolean isTeacherRegister = uri.equals(ctx + "/teacher/register");
        boolean isStatic = uri.startsWith(ctx + "/static/");

        if (isTeacherLogin || isTeacherRegister || isStatic) {
            chain.doFilter(request, response);
            return;
        }

        // Nur Teacher-Bereich schützen
        if (uri.startsWith(ctx + "/teacher/")) {
            HttpSession session = req.getSession(false); // WICHTIG: false, keine neue Session!
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