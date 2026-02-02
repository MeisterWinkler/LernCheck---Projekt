package com.example.quiz.dao;

import com.example.quiz.model.Klass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassDao {

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
}