package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuController – CRUD operations for menu categories and items.
 */
public class MenuController {

    // ── Categories ──────────────────────────────────────────────

    /** Returns all categories. */
    public static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            System.err.println("getAllCategories error: " + e.getMessage());
        }
        return list;
    }

    // ── Menu Items ───────────────────────────────────────────────

    /** Returns all items for a given category, available only. */
    public static List<MenuItem> getItemsByCategory(int categoryId) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT m.id, m.category_id, m.name, m.description, m.price, m.available "
                + "FROM menu_items m WHERE m.category_id = ? AND m.available = 1 ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuItem mi = mapItem(rs);
                    list.add(mi);
                }
            }
        } catch (SQLException e) {
            System.err.println("getItemsByCategory error: " + e.getMessage());
        }
        return list;
    }

    /** Returns ALL menu items (for admin management). */
    public static List<MenuItem> getAllItems() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT m.id, m.category_id, c.name AS cat_name, m.name, "
                + "m.description, m.price, m.available "
                + "FROM menu_items m JOIN categories c ON m.category_id = c.id ORDER BY c.name, m.name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MenuItem mi = mapItem(rs);
                mi.setCategoryName(rs.getString("cat_name"));
                list.add(mi);
            }
        } catch (SQLException e) {
            System.err.println("getAllItems error: " + e.getMessage());
        }
        return list;
    }

    /** Inserts a new menu item. Returns true on success. */
    public static boolean addItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (category_id, name, description, price, available) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCategoryId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getDescription());
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addItem error: " + e.getMessage());
            return false;
        }
    }

    /** Updates an existing menu item. Returns true on success. */
    public static boolean updateItem(MenuItem item) {
        String sql = "UPDATE menu_items SET category_id=?, name=?, description=?, price=?, available=? "
                + "WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCategoryId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getDescription());
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            ps.setInt(6, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateItem error: " + e.getMessage());
            return false;
        }
    }

    /** Deletes a menu item by ID. Returns true on success. */
    public static boolean deleteItem(int itemId) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteItem error: " + e.getMessage());
            return false;
        }
    }

    // ── Private helpers ──────────────────────────────────────────

    private static MenuItem mapItem(ResultSet rs) throws SQLException {
        MenuItem mi = new MenuItem();
        mi.setId(rs.getInt("id"));
        mi.setCategoryId(rs.getInt("category_id"));
        mi.setName(rs.getString("name"));
        mi.setDescription(rs.getString("description"));
        mi.setPrice(rs.getDouble("price"));
        mi.setAvailable(rs.getBoolean("available"));
        return mi;
    }
}
