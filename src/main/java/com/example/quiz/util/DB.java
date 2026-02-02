package com.example.quiz.util;

import jakarta.servlet.ServletContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    public static Connection getConnection(ServletContext ctx) throws SQLException {
        String url = ctx.getInitParameter("db.url");
        String user = ctx.getInitParameter("db.user");
        String pass = ctx.getInitParameter("db.pass");
        return DriverManager.getConnection(url, user, pass);
    }
}