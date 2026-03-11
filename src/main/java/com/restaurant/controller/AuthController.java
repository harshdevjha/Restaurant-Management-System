package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuthController – handles login/logout and session management.
 */
public class AuthController {

    /** Singleton logged-in user for the current session. */
    private static User currentUser = null;

    /**
     * Validates credentials against the database.
     * 
     * @param username the username entered
     * @param password the plaintext password entered
     * @return the matching User, or null if credentials are invalid
     */
    public static User login(String username, String password) {
        String sql = "SELECT id, username, password, full_name, role, active "
                + "FROM users WHERE username = ? AND password = ? AND active = 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getBoolean("active"));
                    currentUser = user;
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    /** Clears the current session. */
    public static void logout() {
        currentUser = null;
    }

    /** Returns the currently logged-in user, or null if not logged in. */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Returns true if a user is currently logged in. */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
