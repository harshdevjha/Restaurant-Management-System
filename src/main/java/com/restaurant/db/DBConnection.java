package com.restaurant.db;

// Importing necessary classes from the java.sql package to handle database connectivity.
import java.sql.Connection;     // Interface that represents a session with a specific database.
import java.sql.DriverManager;  // Class that manages a list of database drivers and establishes connections.
import java.sql.SQLException;   // Exception class that provides information on a database access error or other errors.

/**
 * DBConnection – Singleton utility to manage the MySQL JDBC connection.
 * This class abstracts the logic of loading the MySQL driver and obtaining a connection.
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

    // Connection URL for JDBC. 
    // - useSSL=false: Disables Secure Sockets Layer (SSL) for simpler local development.
    // - allowPublicKeyRetrieval=true: Allows MySQL client to request the public key from the server.
    // - serverTimezone=UTC: Ensures that timestamp data is consistently interpreted in UTC.
    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /** 
     * Returns a new Connection from the DriverManager.
     * Throws an SQLException if the database access error occurs or the URL is null.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Dynamically load the MySQL JDBC driver class into memory at runtime.
            // This registers the driver with the DriverManager.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Use the DriverManager to attempt to establish a connection to the given database URL.
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Caught when the "com.mysql.cj.jdbc.Driver" class is not found in the application's classpath.
            throw new SQLException("MySQL JDBC Driver not found. "
                + "Ensure mysql-connector-j jar is on the classpath.", e);
        }
    }

    /** No-op since connections are no longer explicitly singleton. */
    public static void closeConnection() {
        // Connections are now closed individually by try-with-resources blocks where they are used.
    }

    // Prevent instantiation of this utility class.
    private DBConnection() {}
}
