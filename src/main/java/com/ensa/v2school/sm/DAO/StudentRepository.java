package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
import com.ensa.v2school.sm.Models.DossierAdministratif;
import com.ensa.v2school.sm.Models.Subject;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.*;

public class StudentRepository implements CRUD<Student, String> {

    private DataBaseConnection connection;

    public StudentRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    @Override
    public Student create(Student student) throws SQLException {
        String sql = "INSERT INTO students (id, first_name, last_name, user_id, average, major_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getId());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            if (student.getUser() != null) {
                ps.setInt(4, student.getUser().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setFloat(5, student.getAverage());
            // Ensure major is not null and has an ID
            if (student.getMajor() != null) {
                ps.setInt(6, student.getMajor().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Student not created");
            }
            return student;

        } catch (SQLException e) {
            System.err.println("Error creating student: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Student update(Student student) throws SQLException {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, user_id = ?, average = ?, major_id = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            if (student.getUser() != null) {
                ps.setInt(3, student.getUser().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setFloat(4, student.getAverage());
            // Ensure major is not null and has an ID
            if (student.getMajor() != null) {
                ps.setInt(5, student.getMajor().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, student.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Student not updated");
            }
            return student;

        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Student delete(Student student) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getId());
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Student not deleted");
            }
            return student;

        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Student> get(String id) throws SQLException {
        String sql = """
        SELECT 
            s.id,
            s.first_name,
            s.last_name,
            s.average,
            m.id AS major_id,
            m.name AS major_name,
            m.description AS major_description,
            d.id AS dossier_id,
            d.numero_inscription,
            d.date_creation
        FROM students s
        JOIN majors m ON s.major_id = m.id
        LEFT JOIN dossier_administratif d ON s.id = d.eleve_id
        WHERE s.id = ?
    """;

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // *** ADJUSTED Major constructor: Added new ArrayList<>() for subjects ***
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description"),
                        new ArrayList<>()
                );

                DossierAdministratif dossier = null;
                if (rs.getObject("dossier_id") != null) {
                    dossier = new DossierAdministratif(
                            rs.getInt("dossier_id"),
                            rs.getString("numero_inscription"),
                            rs.getDate("date_creation").toLocalDate(),
                            rs.getString("id"),
                            null
                    );
                }

                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        rs.getFloat("average"),
                        major,
                        dossier
                );
                return Optional.of(student);
            }

        } catch (SQLException e) {
            System.err.println("Error getting student: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    public List<Student> getAll() throws SQLException {
        String sql = """
        SELECT 
            s.id,
            s.first_name,
            s.last_name,
            s.average,
            m.id AS major_id,
            m.name AS major_name,
            m.description AS major_description,
            d.id AS dossier_id,
            d.numero_inscription,
            d.date_creation
        FROM students s
        JOIN majors m ON s.major_id = m.id
        LEFT JOIN dossier_administratif d ON s.id = d.eleve_id
        ORDER BY s.id ASC
        """;

        List<Student> students = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description"),
                        new ArrayList<>()
                );

                DossierAdministratif dossier = null;
                if (rs.getObject("dossier_id") != null) {
                    dossier = new DossierAdministratif(
                            rs.getInt("dossier_id"),
                            rs.getString("numero_inscription"),
                            rs.getDate("date_creation").toLocalDate(),
                            rs.getString("id"),
                            null
                    );
                }

                Student std = new Student(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        rs.getFloat("average"),
                        major,
                        dossier
                );
                students.add(std);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all students: " + e.getMessage());
            throw e;
        }

        return students;
    }

    // CUSTOM METHODS

    public List<Student> findByMajor(int majorId) throws SQLException {
        String sql = """
        SELECT 
            s.id,
            s.first_name,
            s.last_name,
            s.average,
            m.id AS major_id,
            m.name AS major_name,
            m.description AS major_description,
            d.id AS dossier_id,
            d.numero_inscription,
            d.date_creation
        FROM students s
        JOIN majors m ON s.major_id = m.id
        LEFT JOIN dossier_administratif d ON s.id = d.eleve_id
        WHERE s.major_id = ?
    """;

        List<Student> students = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, majorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // *** ADJUSTED Major constructor: Added new ArrayList<>() for subjects ***
                Major major = new Major(
                        rs.getInt("major_id"),
                        rs.getString("major_name"),
                        rs.getString("major_description"),
                        new ArrayList<>()
                );

                DossierAdministratif dossier = null;
                if (rs.getObject("dossier_id") != null) {
                    dossier = new DossierAdministratif(
                            rs.getInt("dossier_id"),
                            rs.getString("numero_inscription"),
                            rs.getDate("date_creation").toLocalDate(),
                            rs.getString("id"),
                            null
                    );
                }

                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        rs.getFloat("average"),
                        major,
                        dossier
                );

                students.add(student);
            }

            return students;

        } catch (SQLException e) {
            System.err.println("Error finding students by major: " + e.getMessage());
            throw e;
        }
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting count: " + e.getMessage());
        }
        return 0;
    }

    public Float getAverage() throws SQLException {
        String sql = "SELECT AVG(average) FROM students";
        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getFloat(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting average: " + e.getMessage());
        }

        return 0f;
    }

    public Map<String, Float> getAverageByMajor() throws SQLException {
        String sql = """
        SELECT m.name, AVG(s.average) AS avg_grade
        FROM students s
        JOIN majors m ON s.major_id = m.id
        GROUP BY m.name
        """;

        Map<String, Float> result = new HashMap<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(
                        rs.getString("name"),
                        rs.getFloat("avg_grade")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error getting average by major: " + e.getMessage());
            throw e;
        }

        return result;
    }
    public void enrollInSubjects(String studentId, List<Integer> subjectIds) throws SQLException {

        String sql = "INSERT INTO student_subject (student_id, subject_id) VALUES (?, ?)";

        try (Connection con = connection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Integer subjectId : subjectIds) {
                    ps.setString(1, studentId);
                    ps.setInt(2, subjectId);
                    ps.addBatch();
                }

                ps.executeBatch();
                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error enrolling student in subjects: " + e.getMessage());
            throw e;
        }
    }


    public List<Subject> getEnrolledSubjects(String studentId) throws SQLException {
        String sql = """
        SELECT s.id, s.name
        FROM subjects s
        JOIN student_subject ss ON s.id = ss.subject_id
        WHERE ss.student_id = ?
        ORDER BY s.name
    """;

        List<Subject> subjects = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject subject = new Subject();
                    subject.setId(rs.getInt("id"));
                    subject.setName(rs.getString("name"));
                    subject.setMajors(new ArrayList<>()); // Empty for now
                    subjects.add(subject);
                }
            }
        }

        return subjects;
    }

    public void unenrollFromSubjects(String studentId, List<Integer> subjectIds) throws SQLException {
        String sql = "DELETE FROM student_subject WHERE student_id = ? AND subject_id = ?";
        Connection con = null;

        try {
            con = connection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Integer subjectId : subjectIds) {
                    ps.setString(1, studentId);
                    ps.setInt(2, subjectId);
                    ps.addBatch();
                }

                ps.executeBatch();
                con.commit();
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error unenrolling student from subjects: " + e.getMessage());
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

}