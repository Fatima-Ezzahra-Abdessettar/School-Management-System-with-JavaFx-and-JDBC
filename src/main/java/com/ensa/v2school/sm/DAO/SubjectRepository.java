package com.ensa.v2school.sm.DAO;
import com.ensa.v2school.sm.Models.Subject;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SubjectRepository implements CRUD<Subject, Integer> {

    private DataBaseConnection connection;
    private MajorRepository majorRepository;

    public SubjectRepository() {
        this.connection = DataBaseConnection.getInstance();
        this.majorRepository = new MajorRepository();
    }

    @Override
    public Subject create(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (name, majorId) VALUES (?, ?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getName());
            ps.setInt(2, subject.getMajor() != null ? subject.getMajor().getId() : 0);

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
        String sql = "UPDATE subjects SET name = ?, majorId = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subject.getName());
            ps.setInt(2, subject.getMajor() != null ? subject.getMajor().getId() : 0);
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
        String sql = "SELECT * FROM subjects WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        null // Major loaded separately
                );

                // Load related Major
                int majorId = rs.getInt("majorId");
                majorRepository.get(majorId).ifPresent(subject::setMajor);

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
        String sql = "SELECT * FROM subjects";
        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        null
                );

                // Load related Major
                int majorId = rs.getInt("majorId");
                majorRepository.get(majorId).ifPresent(subject::setMajor);

                subjects.add(subject);
            }

            return subjects;

        } catch (SQLException e) {
            System.err.println("Error getting all subjects: " + e.getMessage());
            throw e;
        }
    }

    // Custom Methods
    public Optional<List<Subject>> findByMajor(int majorId) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE majorId = ?";
        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        null
                );
                majorRepository.get(majorId).ifPresent(subject::setMajor);
                subjects.add(subject);
            }

            return Optional.of(subjects);

        } catch (SQLException e) {
            System.err.println("Error finding subjects by major: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Subject> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE name = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Subject subject = new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        null
                );

                int majorId = rs.getInt("majorId");
                majorRepository.get(majorId).ifPresent(subject::setMajor);

                return Optional.of(subject);
            }

        } catch (SQLException e) {
            System.err.println("Error finding subject by name: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }
}

