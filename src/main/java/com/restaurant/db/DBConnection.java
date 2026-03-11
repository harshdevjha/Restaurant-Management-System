package com.restaurant.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – Singleton utility to manage the MySQL JDBC connection.
 * Update the constants below to match your local MySQL setup.
 */
public class DBConnection {

    // ── Database configuration ──────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "restaurant_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";           // empty = no password (default on Mac Homebrew install)
    // ────────────────────────────────────────────────────────────

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /** Returns a new Connection from the DriverManager. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. "
                + "Ensure mysql-connector-j jar is on the classpath.", e);
        }
    }

    /** No-op since connections are no longer explicitly singleton. */
    public static void closeConnection() {
        // Connections are now closed individually by try-with-resources.
    }

    // Prevent instantiation
    private DBConnection() {}
}
