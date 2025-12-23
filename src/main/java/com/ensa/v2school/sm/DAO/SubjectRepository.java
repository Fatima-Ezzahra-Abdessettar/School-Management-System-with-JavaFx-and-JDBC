package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectRepository implements CRUD<Subject, Integer> {

    private final DataBaseConnection connection;

    public SubjectRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    /**
     * Helper method to retrieve the list of Majors associated with a given Subject ID.
     */
    private List<Major> getMajorsForSubject(Connection con, int subjectId) throws SQLException {
        String sql = """
            SELECT m.id, m.name, m.description
            FROM majors m
            JOIN major_subject ms ON m.id = ms.major_id
            WHERE ms.subject_id = ?
        """;

        List<Major> majors = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                majors.add(new Major(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        new ArrayList<>()
                ));
            }
        }
        return majors;
    }

    /**
     * Inserts major-subject links into the joining table (part of CREATE/UPDATE).
     */
    private void insertMajors(Connection con, Subject subject) throws SQLException {
        if (subject.getMajors() == null || subject.getMajors().isEmpty()) return;

        String sql = "INSERT INTO major_subject (major_id, subject_id) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Major m : subject.getMajors()) {
                if (m.getId() > 0) {
                    ps.setInt(1, m.getId());
                    ps.setInt(2, subject.getId());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Deletes all major-subject links for a given subject ID (part of UPDATE/DELETE).
     */
    private void deleteMajors(Connection con, int subjectId) throws SQLException {
        String sql = "DELETE FROM major_subject WHERE subject_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.executeUpdate();
        }
    }

    @Override
    public Subject create(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (name) VALUES (?)";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, subject.getName());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        subject.setId(keys.getInt(1));
                    }
                    insertMajors(con, subject);
                    con.commit();
                    return subject;
                }
                con.rollback();
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error creating subject: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Subject update(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET name = ? WHERE id = ?";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, subject.getName());
                ps.setInt(2, subject.getId());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    deleteMajors(con, subject.getId());
                    insertMajors(con, subject);
                    con.commit();
                    return subject;
                }
                con.rollback();
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error updating subject: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Subject delete(Subject subject) throws SQLException {
        String sql = "DELETE FROM subjects WHERE id = ?";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false);

            deleteMajors(con, subject.getId());

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, subject.getId());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    con.commit();
                    return subject;
                }
                con.rollback();
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error deleting subject: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<Subject> get(Integer id) throws SQLException {
        String sql = "SELECT id, name FROM subjects WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return Optional.empty();

            Subject subject = new Subject();
            subject.setId(rs.getInt("id"));
            subject.setName(rs.getString("name"));
            subject.setMajors(getMajorsForSubject(con, id));

            return Optional.of(subject);
        } catch (SQLException e) {
            System.err.println("Error getting subject: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Subject> getAll() throws SQLException {
        String sql = "SELECT id, name FROM subjects ORDER BY id ASC";

        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));
                subjects.add(subject);
            }

            for (Subject subject : subjects) {
                subject.setMajors(getMajorsForSubject(con, subject.getId()));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all subjects: " + e.getMessage());
            throw e;
        }

        return subjects;
    }

    /**
     * Retrieves all Subjects associated with a specific Major ID.
     * FIXED: Now properly loads all majors for each subject, not just an empty list.
     */
    public List<Subject> findByMajorId(int majorId) throws SQLException {
        String sql = """
            SELECT s.id, s.name
            FROM subjects s
            JOIN major_subject ms ON s.id = ms.subject_id
            WHERE ms.major_id = ?
            ORDER BY s.id ASC
        """;

        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));

                // CRITICAL FIX: Load the actual majors for each subject
                subject.setMajors(getMajorsForSubject(con, subject.getId()));

                subjects.add(subject);
            }

        } catch (SQLException e) {
            System.err.println("Error finding subjects by major ID: " + e.getMessage());
            throw e;
        }

        return subjects;
    }
}