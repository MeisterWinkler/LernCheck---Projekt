package com.example.quiz.dao;

import com.example.quiz.model.Klass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDao {

    // ✅ Neu: nur Klassen dieses Lehrers
    public List<Klass> listForTeacher(Connection c, long teacherId) throws Exception {
        String sql = "SELECT id, name FROM classes WHERE teacher_id=? ORDER BY name";
        List<Klass> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Klass k = new Klass();
                    k.setId(rs.getLong("id"));
                    k.setName(rs.getString("name"));
                    out.add(k);
                }
            }
        }
        return out;
    }

    // ✅ Neu: findet Klasse nur wenn sie dem Lehrer gehört
    public Klass findByIdForTeacher(Connection c, long classId, long teacherId) throws Exception {
        String sql = "SELECT id, name FROM classes WHERE id=? AND teacher_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Klass k = new Klass();
                k.setId(rs.getLong("id"));
                k.setName(rs.getString("name"));
                return k;
            }
        }
    }

    // ✅ Neu: Klasse anlegen (teacher_id Pflicht)
    public long create(Connection c, long teacherId, String name) throws Exception {
        String sql = "INSERT INTO classes(teacher_id, name) VALUES (?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, teacherId);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    // ============================================================
    // ✅ Kompatibilitätsschicht (damit alte Servlets nicht mehr rot sind)
    // ============================================================

    /**
     * Früher gab es listAll(Connection). Das darf es für die Logik eigentlich nicht mehr geben,
     * weil sonst Lehrer fremde Klassen sehen würden.
     * Deshalb werfen wir hier bewusst eine Exception – und korrigieren alle Servlets unten.
     * Damit du sofort siehst wo es noch verwendet wird.
     */
    public List<Klass> listAll(Connection c) throws Exception {
        throw new UnsupportedOperationException("listAll() ist deaktiviert. Nutze listForTeacher(c, teacherId).");
    }

    /**
     * Früher gab es findById(Connection,long). Das ist unsicher ohne teacherId.
     */
    public Klass findById(Connection c, long classId) throws Exception {
        throw new UnsupportedOperationException("findById() ist deaktiviert. Nutze findByIdForTeacher(c, classId, teacherId).");
    }
}