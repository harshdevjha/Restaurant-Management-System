package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminController – Provides CRUD operations specifically for managing system User accounts.
 * These methods interact with the 'users' database table and are intended to be accessed 
 * only by users with an 'ADMIN' role.
 */
public class AdminController {

    /** 
     * Retrieves all user accounts (both STAFF and ADMIN).
     * @return A list containing all User objects found in the database.
     */
    public static List<User> getAllStaff() {
        List<User> list = new ArrayList<>();
        // Query fetching all properties to fully populate the User model, sorted by role then name.
        String sql = "SELECT id, username, password, full_name, role, active FROM users ORDER BY role, full_name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Iterate over the result set to map rows to User objects
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getBoolean("active")
                ));
            }
        } catch (SQLException e) {
            System.err.println("getAllStaff error: " + e.getMessage());
        }
        return list;
    }

    /** 
     * Adds a newly registered staff or admin account to the database.
     * @param user The User model containing the new details to be inserted.
     * @return true if the database insertion was successful, false otherwise. 
     */
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            // Substitute the ? placeholders with object values
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            
            // executeUpdate returns the number of inserted rows. > 0 means success.
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addUser error: " + e.getMessage());
            return false;
        }
    }

    /** 
     * Updates an existing user record based on its ID.
     * @param user The User model populated with updated fields and the target ID.
     * @return true if the modification succeeded, false otherwise.
     */
    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, role=?, active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getId());  // Use ID to identify which row to update
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateUser error: " + e.getMessage());
            return false;
        }
    }

    /** 
     * Permanently deletes a user account from the database.
     * @param userId The unique ID of the specific user to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, userId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteUser error: " + e.getMessage());
            return false;
        }
    }
}
