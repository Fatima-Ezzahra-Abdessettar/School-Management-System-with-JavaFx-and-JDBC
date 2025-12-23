package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectRepository implements CRUD<Subject, Integer> {

    private DataBaseConnection connection;

    public SubjectRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    @Override
    public Subject create(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (name, major_id) VALUES (?, ?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getName());
            ps.setInt(2, subject.getMajor().getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    subject.setId(generatedKeys.getInt(1));
                }
                return subject;
            }

        } catch (SQLException e) {
            System.err.println("Error creating subject: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Subject update(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET name = ?, major_id = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subject.getName());
            ps.setInt(2, subject.getMajor().getId());
            ps.setInt(3, subject.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0 ? subject : null;

        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Subject delete(Subject subject) throws SQLException {
        String sql = "DELETE FROM subjects WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subject.getId());
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0 ? subject : null;

        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Subject> get(Integer id) throws SQLException {
        String sql = """
            SELECT 
                s.id,
                s.name,
                m.id AS major_id,
                m.name AS major_name,
                m.description AS major_description
            FROM subjects s
            JOIN majors m ON s.major_id = m.id
            WHERE s.id = ?
        """;

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description")
                );
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        major
                );
                return Optional.of(subject);
            }

        } catch (SQLException e) {
            System.err.println("Error getting subject: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<Subject> getAll() throws SQLException {
        String sql = """
            SELECT 
                s.id,
                s.name,
                m.id AS major_id,
                m.name AS major_name,
                m.description AS major_description
            FROM subjects s
            JOIN majors m ON s.major_id = m.id
            ORDER BY s.id ASC
        """;

        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description")
                );
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        major
                );
                subjects.add(subject);
            }

            return subjects;

        } catch (SQLException e) {
            System.err.println("Error getting all subjects: " + e.getMessage());
            throw e;
        }
    }

    // Custom Methods
    public List<Subject> findByMajor(int majorId) throws SQLException {
        String sql = """
            SELECT 
                s.id,
                s.name,
                m.id AS major_id,
                m.name AS major_name,
                m.description AS major_description
            FROM subjects s
            JOIN majors m ON s.major_id = m.id
            WHERE s.major_id = ?
            ORDER BY s.name ASC
        """;

        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description")
                );
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        major
                );
                subjects.add(subject);
            }

            return subjects;

        } catch (SQLException e) {
            System.err.println("Error finding subjects by major: " + e.getMessage());
            throw e;
        }
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting subjects count: " + e.getMessage());
            throw e;
        }
        return 0;
    }
}