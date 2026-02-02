package com.example.quiz.dao;

import com.example.quiz.model.Klass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassDao {

    /**
     * Liste aller Klassen (für Dropdown etc.)
     * (Falls du nur teacher-spezifische Klassen willst, nutze listForTeacher)
     */
    public List<Klass> listAll(Connection c) throws Exception {
        String sql = "SELECT id, name FROM classes ORDER BY name";
        List<Klass> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Klass k = new Klass();
                k.setId(rs.getLong("id"));
                k.setName(rs.getString("name"));
                out.add(k);
            }
        }
        return out;
    }

    /**
     * ✅ FIX: Diese Methode wird vom TeacherDashboardServlet erwartet.
     * Falls du Klassen NICHT teacher-spezifisch speicherst, liefert sie einfach alle Klassen zurück.
     * Wenn du später teacher_id in der classes-Tabelle einführst, kannst du hier filtern.
     */
    public List<Klass> listForTeacher(Connection c, long teacherId) throws Exception {
        // Aktuell: alle Klassen (weil classes typischerweise global sind)
        // Falls es bei dir teacher_id gibt, ersetze Query z.B.:
        // SELECT id,name FROM classes WHERE teacher_id=? ORDER BY name
        return listAll(c);
    }

    public Klass findById(Connection c, long id) throws Exception {
        String sql = "SELECT id, name FROM classes WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Klass k = new Klass();
                k.setId(rs.getLong("id"));
                k.setName(rs.getString("name"));
                return k;
            }
        }
    }

    public long create(Connection c, String name) throws Exception {
        String sql = "INSERT INTO classes(name) VALUES (?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}