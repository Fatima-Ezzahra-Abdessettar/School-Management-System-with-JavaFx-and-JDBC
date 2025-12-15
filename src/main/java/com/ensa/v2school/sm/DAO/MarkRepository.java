package com.ensa.v2school.sm.DAO;
import com.ensa.v2school.sm.Models.Mark;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class MarkRepository implements CRUD<Mark, Integer> {

    private DataBaseConnection connection;
    private StudentRepository studentRepository;
    private SubjectRepository subjectRepository;

    public MarkRepository() {
        this.connection = DataBaseConnection.getInstance();
        this.studentRepository = new StudentRepository();
        this.subjectRepository = new SubjectRepository();
    }

    @Override
    public Mark create(Mark mark) throws SQLException {
        String sql = "INSERT INTO marks (studentId, subjectId, value) VALUES (?, ?, ?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, mark.getStudent() != null ? mark.getStudent().getId() : null);
            ps.setInt(2, mark.getSubject() != null ? mark.getSubject().getId() : 0);
            ps.setFloat(3, mark.getValue());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    mark.setId(generatedKeys.getInt(1));
                }
                return mark;
            }

        } catch (SQLException e) {
            System.err.println("Error creating mark: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Mark update(Mark mark) throws SQLException {
        String sql = "UPDATE marks SET studentId = ?, subjectId = ?, value = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mark.getStudent() != null ? mark.getStudent().getId() : null);
            ps.setInt(2, mark.getSubject() != null ? mark.getSubject().getId() : 0);
            ps.setFloat(3, mark.getValue());
            ps.setInt(4, mark.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0 ? mark : null;

        } catch (SQLException e) {
            System.err.println("Error updating mark: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Mark delete(Mark mark) throws SQLException {
        String sql = "DELETE FROM marks WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, mark.getId());
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0 ? mark : null;

        } catch (SQLException e) {
            System.err.println("Error deleting mark: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Mark> get(Integer id) throws SQLException {
        String sql = "SELECT * FROM marks WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Mark mark = new Mark(
                        rs.getInt("id"),
                        null, // Student loaded separately
                        null, // Subject loaded separately
                        rs.getFloat("value")
                );

                // Load related objects - CHANGED: getString instead of getInt
                String studentId = rs.getString("studentId");
                int subjectId = rs.getInt("subjectId");

                studentRepository.get(studentId).ifPresent(mark::setStudent);
                subjectRepository.get(subjectId).ifPresent(mark::setSubject);

                return Optional.of(mark);
            }

        } catch (SQLException e) {
            System.err.println("Error getting mark: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<Mark> getAll() throws SQLException {
        String sql = "SELECT * FROM marks";
        List<Mark> marks = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Mark mark = new Mark(
                        rs.getInt("id"),
                        null,
                        null,
                        rs.getFloat("value")
                );

                // CHANGED: getString instead of getInt
                String studentId = rs.getString("studentId");
                int subjectId = rs.getInt("subjectId");

                studentRepository.get(studentId).ifPresent(mark::setStudent);
                subjectRepository.get(subjectId).ifPresent(mark::setSubject);

                marks.add(mark);
            }

            return marks;

        } catch (SQLException e) {
            System.err.println("Error getting all marks: " + e.getMessage());
            throw e;
        }
    }

    // Custom Methods
    // CHANGED: Parameter type from int to String
    public Optional<List<Mark>> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT * FROM marks WHERE studentId = ?";
        List<Mark> marks = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // CHANGED: setString instead of setInt
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Mark mark = new Mark(
                        rs.getInt("id"),
                        null,
                        null,
                        rs.getFloat("value")
                );

                studentRepository.get(studentId).ifPresent(mark::setStudent);
                subjectRepository.get(rs.getInt("subjectId")).ifPresent(mark::setSubject);

                marks.add(mark);
            }

            return Optional.of(marks);

        } catch (SQLException e) {
            System.err.println("Error finding marks by student: " + e.getMessage());
            throw e;
        }
    }

    public Optional<List<Mark>> findBySubject(int subjectId) throws SQLException {
        String sql = "SELECT * FROM marks WHERE subjectId = ?";
        List<Mark> marks = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Mark mark = new Mark(
                        rs.getInt("id"),
                        null,
                        null,
                        rs.getFloat("value")
                );

                // CHANGED: getString instead of getInt
                studentRepository.get(rs.getString("studentId")).ifPresent(mark::setStudent);
                subjectRepository.get(subjectId).ifPresent(mark::setSubject);

                marks.add(mark);
            }

            return Optional.of(marks);

        } catch (SQLException e) {
            System.err.println("Error finding marks by subject: " + e.getMessage());
            throw e;
        }
    }

    // CHANGED: Parameter type from int to String
    public Optional<Mark> findByStudentAndSubject(String studentId, int subjectId) throws SQLException {
        String sql = "SELECT * FROM marks WHERE studentId = ? AND subjectId = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // CHANGED: setString instead of setInt
            ps.setString(1, studentId);
            ps.setInt(2, subjectId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Mark mark = new Mark(
                        rs.getInt("id"),
                        null,
                        null,
                        rs.getFloat("value")
                );

                studentRepository.get(studentId).ifPresent(mark::setStudent);
                subjectRepository.get(subjectId).ifPresent(mark::setSubject);

                return Optional.of(mark);
            }

        } catch (SQLException e) {
            System.err.println("Error finding mark by student and subject: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    // Calculate student average
    // CHANGED: Parameter type from int to String
    public float calculateStudentAverage(String studentId) throws SQLException {
        String sql = "SELECT AVG(value) as average FROM marks WHERE studentId = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // CHANGED: setString instead of setInt
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getFloat("average");
            }

        } catch (SQLException e) {
            System.err.println("Error calculating student average: " + e.getMessage());
            throw e;
        }

        return 0.0f;
    }
}