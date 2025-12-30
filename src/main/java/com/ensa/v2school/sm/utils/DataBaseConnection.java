package com.ensa.v2school.sm.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static DataBaseConnection instance;
    private final String url="jdbc:mysql://localhost:3306/school_management";
    private final String user="root";
    private final String password="mamapapa123";
    private Connection connection;
    private DataBaseConnection() {
        try{
            connection = DriverManager.getConnection(url,user,password);

        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            throw new RuntimeException(e);
        }
    }
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
    public static DataBaseConnection getInstance() {
        if (instance == null) {
            instance = new DataBaseConnection();
        }
        return instance;
    }
}
