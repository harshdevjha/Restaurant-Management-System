package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuthController – Handles user authentication (login/logout) and maintains the active session state.
 * This class abstracts database validation queries for user login.
 */
public class AuthController {

    /** 
     * Static field to hold the currently logged-in User instance for the session.
     * Null indicates no user is currently authenticated.
     */
    private static User currentUser = null;

    /**
     * Authenticates a user against the 'users' table in the database.
     * 
     * @param username the username entered in the UI
     * @param password the password entered in the UI (currently plaintext matching)
     * @return the fully populated User object if credentials match and the account is active,
     *         otherwise returns null.
     */
    public static User login(String username, String password) {
        // Query to check credentials; ensures the account is also flagged as active (=1).
        String sql = "SELECT id, username, password, full_name, role, active "
                + "FROM users WHERE username = ? AND password = ? AND active = 1";
                
        // Try-with-resources statement ensures Connection and PreparedStatement are closed automatically
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind the parameterized query values
            ps.setString(1, username);
            ps.setString(2, password);
            
            // Execute query and try matching the result set
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Map the matching record to a new User model object
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getBoolean("active"));
                            
                    // Set the global session user to the authenticated user
                    currentUser = user;
                    return user;
                }
            }
        } catch (SQLException e) {
            // Log authentication errors to standard error stream
            System.err.println("Login error: " + e.getMessage());
        }
        
        // Return null if no matching active user was found or an exception occurred
        return null;
    }

    /** 
     * Clears the current session by setting currentUser back to null. 
     */
    public static void logout() {
        currentUser = null;
    }

    /** 
     * Retrieves the currently authenticated user session.
     * @return the User currently logged in, or null if none.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** 
     * Quick utility check to see if a session is currently active.
     * @return true if a user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
