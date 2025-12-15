package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MajorRepository implements CRUD<Major, Integer> {

    private DataBaseConnection connection;

    public MajorRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    @Override
    public Major create(Major major) throws SQLException {
        String sql = "INSERT INTO majors (name) VALUES (?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, major.getMajorName());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    major.setId(generatedKeys.getInt(1));
                }
                return major;
            }

        } catch (SQLException e) {
            System.err.println("Error creating major: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Major update(Major major) throws SQLException {
        String sql = "UPDATE majors SET name = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, major.getMajorName());
            ps.setInt(2, major.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0 ? major : null;

        } catch (SQLException e) {
            System.err.println("Error updating major: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Major delete(Major major) throws SQLException {
        String sql = "DELETE FROM majors WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, major.getId());
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0 ? major : null;

        } catch (SQLException e) {
            System.err.println("Error deleting major: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Major> get(Integer id) throws SQLException {
        String sql = "SELECT * FROM majors WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Major major = new Major(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                return Optional.of(major);
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

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Major major = new Major(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                majors.add(major);
            }

            return majors;

        } catch (SQLException e) {
            System.err.println("Error getting all majors: " + e.getMessage());
            throw e;
        }
    }

    // Custom Methods
    public Optional<Major> findByName(String majorName) throws SQLException {
        String sql = "SELECT * FROM majors WHERE name = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, majorName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Major major = new Major(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                return Optional.of(major);
            }

        } catch (SQLException e) {
            System.err.println("Error finding major by name: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM majors";
        try{
            Connection con = connection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("Error getting majors: " + e.getMessage());
        }
        return 0;
    }
}
