package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MajorRepository implements CRUD<Major, Integer> {

    private final DataBaseConnection connection;

    public MajorRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    /**
     * Helper method to map a ResultSet row to a Major object (without subjects).
     */
    private Major mapResultSetToMajor(ResultSet rs) throws SQLException {
        return new Major(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                new ArrayList<>() // Initialize empty subjects list
        );
    }

    /**
     * Loads the list of subjects associated with a specific Major ID.
     */
    private List<Subject> loadSubjectsForMajor(int majorId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.id, s.name FROM subjects s " +
                "JOIN major_subject ms ON s.id = ms.subject_id " +
                "WHERE ms.major_id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Note: We don't load the List<Major> inside Subject here to avoid infinite recursion/deep loading
                    Subject subject = new Subject(
                            rs.getInt("id"),
                            rs.getString("name"),
                            new ArrayList<>() // Empty list for now
                    );
                    subjects.add(subject);
                }
            }
        }
        return subjects;
    }

    @Override
    public Major create(Major major) throws SQLException {
        String sql = "INSERT INTO majors (name, description) VALUES (?, ?)";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, major.getMajorName());
                ps.setString(2, major.getDescription());

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        major.setId(generatedKeys.getInt(1));
                        insertMajorSubjects(con, major.getId(), major.getSubjects());
                    }
                    con.commit(); // Commit transaction
                    return major;
                }
                con.rollback(); // Rollback if creation failed
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error creating major: " + e.getMessage());
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
    public Major update(Major major) throws SQLException {
        String sql = "UPDATE majors SET name = ?, description = ? WHERE id = ?";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, major.getMajorName());
                ps.setString(2, major.getDescription());
                ps.setInt(3, major.getId());

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    updateMajorSubjects(con, major.getId(), major.getSubjects());
                    con.commit(); // Commit transaction
                    return major;
                }
                con.rollback(); // Rollback if update failed
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error updating major: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Restore auto-commit
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }
    @Override
    public Major delete(Major major) throws SQLException {
        String sql = "DELETE FROM majors WHERE id = ?";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 1. Delete links in the joining table
            deleteMajorSubjects(con, major.getId());

            // 2. Delete the major itself
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, major.getId());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    con.commit(); // Commit transaction
                    return major;
                }
                con.rollback(); // Rollback if delete failed
                return null;
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error deleting major: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Restore auto-commit
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<Major> get(Integer id) throws SQLException {
        String sql = "SELECT * FROM majors WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Major major = mapResultSetToMajor(rs);
                    // Load associated subjects
                    major.setSubjects(loadSubjectsForMajor(major.getId()));
                    return Optional.of(major);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting major: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<Major> getAll() throws SQLException {
        String sql = "SELECT * FROM majors";
        List<Major> majors = new ArrayList<>();
        Map<Integer, Major> majorMap = new HashMap<>(); // To quickly access majors

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Major major = mapResultSetToMajor(rs);
                majors.add(major);
                majorMap.put(major.getId(), major);
            }

            // Load subjects for all fetched majors in a single pass/query (Batch loading for efficiency)
            if (!majors.isEmpty()) {
                loadAllSubjects(con, majorMap);
            }

            return majors;

        } catch (SQLException e) {
            System.err.println("Error getting all majors: " + e.getMessage());
            throw e;
        }
    }

    // --- Private Methods for Many-to-Many Relationship Management ---

    /**
     * Deletes all subject links for a given major ID.
     */
    private void deleteMajorSubjects(Connection con, int majorId) throws SQLException {
        String sql = "DELETE FROM major_subject WHERE major_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, majorId);
            ps.executeUpdate();
        }
    }

    /**
     * Inserts new subject links for a given major ID.
     */
    private void insertMajorSubjects(Connection con, int majorId, List<Subject> subjects) throws SQLException {
        if (subjects == null || subjects.isEmpty()) return;

        String sql = "INSERT INTO major_subject (major_id, subject_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Subject subject : subjects) {
                // **Important**: Assumes the Subject object already has a valid ID
                if (subject.getId() > 0) {
                    ps.setInt(1, majorId);
                    ps.setInt(2, subject.getId());
                    ps.addBatch();
                } else {
                    // Handle case where Subject ID is missing (e.g., log error or throw exception)
                    System.err.println("Warning: Skipping subject with missing ID: " + subject.getName());
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Clears and re-inserts the subject links for an updated major.
     */
    private void updateMajorSubjects(Connection con, int majorId, List<Subject> subjects) throws SQLException {
        deleteMajorSubjects(con, majorId);
        insertMajorSubjects(con, majorId, subjects);
    }

    /**
     * Loads subjects for all majors found in the map (efficient batch loading).
     * This is an advanced approach to avoid N+1 queries.
     */
    private void loadAllSubjects(Connection con, Map<Integer, Major> majorMap) throws SQLException {
        // Query to get all major-subject links for the major IDs we just fetched
        String sql = "SELECT ms.major_id, s.id AS subject_id, s.name AS subject_name " +
                "FROM major_subject ms " +
                "JOIN subjects s ON ms.subject_id = s.id " +
                "WHERE ms.major_id IN (" +
                String.join(",", java.util.Collections.nCopies(majorMap.size(), "?")) +
                ")";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int index = 1;
            for (Integer id : majorMap.keySet()) {
                ps.setInt(index++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int majorId = rs.getInt("major_id");
                    Major major = majorMap.get(majorId);

                    if (major != null) {
                        Subject subject = new Subject(
                                rs.getInt("subject_id"),
                                rs.getString("subject_name"),
                                new ArrayList<>() // Keep its list of majors empty for now
                        );
                        major.getSubjects().add(subject);
                    }
                }
            }
        }
    }

    // --- Original Custom Methods (Kept for completeness) ---

    public Optional<Major> findByName(String majorName) throws SQLException {
        String sql = "SELECT * FROM majors WHERE name = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, majorName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Major major = mapResultSetToMajor(rs);
                    // Load associated subjects
                    major.setSubjects(loadSubjectsForMajor(major.getId()));
                    return Optional.of(major);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding major by name: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM majors";
        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting majors count: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    /**
     * Checks if a major has any students enrolled.
     * @param majorId The ID of the major to check
     * @return true if the major has students, false otherwise
     */
    public boolean hasStudents(int majorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE major_id = ?";
        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if major has students: " + e.getMessage());
            throw e;
        }
        return false;
    }
}