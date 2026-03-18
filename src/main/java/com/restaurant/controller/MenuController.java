package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuController – Implements the database operations (CRUD) for the restaurant's menu structure.
 * This encapsulates both Category manipulation and MenuItem tracking.
 */
public class MenuController {

    // ── Categories ──────────────────────────────────────────────

    /** 
     * Retrieves all available menu categories from the database.
     * @return A list of Category objects used to populate UI components like tabs or dropdowns.
     */
    public static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            
            // Loop through the results to map relational data into object-oriented models
            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("id"), 
                        rs.getString("name"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            System.err.println("getAllCategories error: " + e.getMessage());
        }
        return list;
    }

    // ── Menu Items ───────────────────────────────────────────────

    /** 
     * Retrieves all dishes/beverages mapped to a specific category that are currently marked as available.
     * Useful when building the customer-facing ordering screen.
     * @param categoryId the primary key of the target category.
     * @return A list of available MenuItem objects.
     */
    public static List<MenuItem> getItemsByCategory(int categoryId) {
        List<MenuItem> list = new ArrayList<>();
        // Query to fetch items specifically filtering out unavailable (out-of-stock) items.
        String sql = "SELECT m.id, m.category_id, m.name, m.description, m.price, m.available "
                + "FROM menu_items m WHERE m.category_id = ? AND m.available = 1 ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Extract row mapping logic into helper method to reduce redundancy
                    MenuItem mi = mapItem(rs);
                    list.add(mi);
                }
            }
        } catch (SQLException e) {
            System.err.println("getItemsByCategory error: " + e.getMessage());
        }
        return list;
    }

    /** 
     * Retrieves ALL menu items irrespective of availability status.
     * This query also joins with the Categories table to fetch the human-readable category name.
     * Typically used for administrative backend views.
     * @return A list of all MenuItem objects with mapped category names.
     */
    public static List<MenuItem> getAllItems() {
        List<MenuItem> list = new ArrayList<>();
        // INNER JOIN connects menu_items with categories on their referencing keys
        String sql = "SELECT m.id, m.category_id, c.name AS cat_name, m.name, "
                + "m.description, m.price, m.available "
                + "FROM menu_items m JOIN categories c ON m.category_id = c.id ORDER BY c.name, m.name";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MenuItem mi = mapItem(rs);
                // The category name retrieved through the JOIN is set separately
                mi.setCategoryName(rs.getString("cat_name"));
                list.add(mi);
            }
        } catch (SQLException e) {
            System.err.println("getAllItems error: " + e.getMessage());
        }
        return list;
    }

    /** 
     * Inserts a newly defined menu item into the database.
     * @param item the configured MenuItem object to add.
     * @return true if successful, false otherwise.
     */
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

    /** 
     * Commits modifications of an existing menu item (e.g. price change, marking unavailable) to the DB.
     * @param item the MenuItem explicitly containing the Target ID.
     * @return true if an update was successfully applied.
     */
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

    /** 
     * Permanently deletes a menu item. 
     * NOTE: Be cautious as deleting items that are referenced by older orders will cascade 
     * if the schema is set up with ON DELETE CASCADE, potentially altering historical data.
     * @param itemId the explicit item ID to remove.
     * @return true if the deletion successfully fired.
     */
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

    /**
     * Reusable private helper to instantiate and map a MenuItem object from a JDBC ResultSet.
     * Prevents duplication across retrieval methods.
     * @param rs The active, iterated ResultSet.
     * @throws SQLException If any mapped properties are missing in the resultSet.
     */
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
