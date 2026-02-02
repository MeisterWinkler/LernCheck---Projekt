package com.example.quiz.util;

import jakarta.servlet.ServletContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    public static Connection getConnection(ServletContext ctx) throws SQLException {
        String driver = ctx.getInitParameter("db.driver");
        String url = ctx.getInitParameter("db.url");
        String user = ctx.getInitParameter("db.user");
        String pass = ctx.getInitParameter("db.pass");

        if (url == null || url.isBlank()) {
            throw new SQLException("DB URL fehlt (web.xml context-param db.url).");
        }

        // Treiber explizit laden
        try {
            if (driver == null || driver.isBlank()) {
                driver = "org.mariadb.jdbc.Driver";
            }
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MariaDB JDBC Driver nicht im Classpath gefunden: " + driver +
                            "\n-> Prüfe, ob mariadb-java-client im WEB-INF/lib der WAR ist.",
                    e
            );
        }

        return DriverManager.getConnection(url, user, pass);
    }
}