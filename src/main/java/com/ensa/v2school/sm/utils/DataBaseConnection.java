package com.ensa.v2school.sm.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    static DataBaseConnection instance;
    private static final String url="jdbc:mysql://localhost:3306/school_management";
    private static final String user="root";
    private static final String password="mamapapa123";
    Connection connection;
    private DataBaseConnection(String url, String user, String password) {
        try{
            connection = DriverManager.getConnection(url,user,password);
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            throw new RuntimeException(e);
        }
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    public static synchronized DataBaseConnection getInstance() {
        if (instance == null) {
            return new DataBaseConnection(url, user, password);
        }
        else {
        return instance; }
    }
}
