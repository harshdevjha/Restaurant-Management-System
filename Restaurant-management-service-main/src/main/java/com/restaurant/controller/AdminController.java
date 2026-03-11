package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminController – manages staff accounts (Admin-only operations).
 */
public class AdminController {

    /** Returns all user accounts. */
    public static List<User> getAllStaff() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, role, active FROM users ORDER BY role, full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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

    /** Adds a new staff/admin account. Returns true on success. */
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addUser error: " + e.getMessage());
            return false;
        }
    }

    /** Updates an existing user account. Returns true on success. */
    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, role=?, active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateUser error: " + e.getMessage());
            return false;
        }
    }

    /** Deletes a user account by ID. Returns true on success. */
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
