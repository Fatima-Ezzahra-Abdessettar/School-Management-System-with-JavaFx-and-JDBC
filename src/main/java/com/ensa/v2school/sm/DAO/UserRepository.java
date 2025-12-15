package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.ROLE;
import com.ensa.v2school.sm.Models.User;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements CRUD<User, Integer> {

    private static DataBaseConnection connection;

    public UserRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    @Override
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (userName, password, role) VALUES (?, ?, ?)";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw e;
        }

        return null;
    }


    public User update(User user) throws SQLException {
        String sql = "UPDATE users SET userName = ?, password = ?, role = ? WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());
            ps.setInt(4, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0 ? user : null;

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public User delete(User user) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, user.getId());
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0 ? user : null;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<User> get(Integer id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        ROLE.valueOf(rs.getString("role"))
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<User> getAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        ROLE.valueOf(rs.getString("role"))
                );
                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            throw e;
        }
    }

    // Custom Methods
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE userName = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        ROLE.valueOf(rs.getString("role"))
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<List<User>> findByRole(ROLE role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ?";
        List<User> users = new ArrayList<>();

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        ROLE.valueOf(rs.getString("role"))
                );
                users.add(user);
            }

            return Optional.of(users);

        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
            throw e;
        }
    }

    // Authentication method
    public Optional<User> authenticate(String username, String password) throws SQLException {
        Optional<User> userOpt = findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In production, use proper password hashing!
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}