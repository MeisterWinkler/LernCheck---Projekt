package com.example.quiz.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        // ✅ Öffentlich (ohne Login)
        boolean isPublic =
                uri.equals(ctx + "/") ||
                        uri.equals(ctx + "/teacher/login") ||
                        uri.equals(ctx + "/teacher/register") ||
                        uri.startsWith(ctx + "/student/") ||
                        uri.startsWith(ctx + "/static/");

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Alles unter /teacher/* braucht teacherId in Session
        if (uri.startsWith(ctx + "/teacher/")) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("teacherId") == null) {
                resp.sendRedirect(ctx + "/teacher/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}