package com.example.quiz.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        String path = r.getRequestURI();
        if (path.endsWith("/teacher/login")) {
            chain.doFilter(req, res);
            return;
        }

        Object teacherId = r.getSession().getAttribute("teacherId");
        if (teacherId == null) {
            w.sendRedirect(r.getContextPath() + "/teacher/login");
            return;
        }
        chain.doFilter(req, res);
    }
}