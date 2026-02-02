package com.example.quiz.dao;

import com.example.quiz.model.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TeacherDao {
    public Teacher findByUsername(Connection c, String username) throws Exception {
        String sql = "SELECT id, username, password_hash FROM teachers WHERE username=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Teacher t = new Teacher();
                t.id = rs.getLong("id");
                t.username = rs.getString("username");
                t.passwordHash = rs.getString("password_hash");
                return t;
            }
        }
    }
}