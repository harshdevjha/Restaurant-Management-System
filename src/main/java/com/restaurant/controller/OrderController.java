package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.RestaurantTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderController – Manages the transactional lifecycle of customer orders and 
 * the occupancy state of restaurant tables. Connects multiple tables ('orders', 
 * 'order_items', 'restaurant_tables').
 */
public class OrderController {

    // ── Tables ───────────────────────────────────────────────────

    /** 
     * Retrieves all physical restaurant tables from the database.
     * @return List of RestaurantTable representing the current capacity and status of the dining area. 
     */
    public static List<RestaurantTable> getAllTables() {
        List<RestaurantTable> list = new ArrayList<>();
        String sql = "SELECT id, number, capacity, status FROM restaurant_tables ORDER BY number";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RestaurantTable(
                        rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity"),
                        RestaurantTable.Status.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            System.err.println("getAllTables error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates the occupancy state (FREE or OCCUPIED) of a specific table.
     * @param tableId the primary key of the table in the database.
     * @param status the new Status to apply.
     * @throws SQLException escalates potential DB failures to the caller.
     */
    public static void updateTableStatus(int tableId, RestaurantTable.Status status) throws SQLException {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, tableId);
            ps.executeUpdate();
        }
    }

    // ── Orders ───────────────────────────────────────────────────

    /**
     * Instantiates a new order session with 'PENDING' status and marks the corresponding table as OCCUPIED.
     * 
     * @param tableId the DB ID of the physical table
     * @param userId  the DB ID of the staff member placing the order
     * @return the auto-generated primary key (order ID) of the new order, or -1 on failure.
     */
    public static int createOrder(int tableId, int userId) {
        String sql = "INSERT INTO orders (table_id, user_id, status) VALUES (?, ?, 'PENDING')";
        // Request the generated keys so we can return the newly created Order ID
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            
            // Retrieve the auto-incremented primary key assigned by MySQL
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    // Synchronously update the table status to OCCUPIED
                    updateTableStatus(tableId, RestaurantTable.Status.OCCUPIED);
                    return orderId;
                }
            }
        } catch (SQLException e) {
            System.err.println("createOrder error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Adds a new menu item to an active order or increments the quantity if the item is already present.
     * 
     * @param orderId    the parent Order ID
     * @param menuItemId the specific dish ID
     * @param itemName   convenience parameter (not stored in this junction table directly)
     * @param unitPrice  the historical cost to lock in the price of this item for this order
     * @param quantity   number of portions ordered
     * @return true if the DB transaction succeeded
     */
    public static boolean addItemToOrder(int orderId, int menuItemId, String itemName,
            double unitPrice, int quantity) {
            
        // First, check if this specific dish is already tracked under this order session
        String checkSql = "SELECT id, quantity FROM order_items WHERE order_id=? AND menu_item_id=?";
        String updateSql = "UPDATE order_items SET quantity = quantity + ? WHERE id = ?";
        String insertSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";
                
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, menuItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // The item is already in the order, just increment the quantity
                        int existingId = rs.getInt("id");
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, quantity);
                            upd.setInt(2, existingId);
                            return upd.executeUpdate() > 0;
                        }
                    }
                }
            }
            // If the item wasn't in the order, insert a brand new row
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, menuItemId);
                ps.setInt(3, quantity);
                ps.setDouble(4, unitPrice);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("addItemToOrder error: " + e.getMessage());
            return false;
        }
    }

    /** 
     * Deletes a specific order item line completely from the database.
     * @param orderItemId the explicit ID within the 'order_items' table.
     * @return true on success.
     */
    public static boolean removeOrderItem(int orderItemId) {
        String sql = "DELETE FROM order_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("removeOrderItem error: " + e.getMessage());
            return false;
        }
    }

    /** 
     * Overwrites the quantity of an existing order line item. 
     * If the quantity drops to 0 or below, the item is removed to maintain data integrity.
     */
    public static boolean updateOrderItemQuantity(int orderItemId, int newQuantity) {
        if (newQuantity <= 0)
            return removeOrderItem(orderItemId);
            
        String sql = "UPDATE order_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, orderItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateOrderItemQuantity error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Looks up the currently active order (either PENDING or SERVED) associated with a specific table.
     * This query uses JOINs to densely pack related user and table info into the returned Order object.
     * @param tableId the reference table identifier
     * @return the active Order object fully populated with its items, or null if the table is FREE.
     */
    public static Order getActiveOrderForTable(int tableId) {
        String sql = "SELECT o.id, o.table_id, t.number AS table_number, o.user_id, "
                + "u.full_name AS user_name, o.status, o.created_at "
                + "FROM orders o "
                + "JOIN restaurant_tables t ON o.table_id = t.id "
                + "JOIN users u ON o.user_id = u.id "
                + "WHERE o.table_id = ? AND o.status IN ('PENDING','SERVED') "
                + "ORDER BY o.created_at DESC LIMIT 1";
                
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    // Crucially, immediately fetch all nested menu line items
                    order.setItems(getOrderItems(order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("getActiveOrderForTable error: " + e.getMessage());
        }
        return null; // Return null if there are no active orders for this table
    }

    /** 
     * Retrieves all comprehensive historical orders (usually for analytical/admin overview panes). 
     */
    public static List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.table_id, t.number AS table_number, o.user_id, "
                + "u.full_name AS user_name, o.status, o.created_at "
                + "FROM orders o "
                + "JOIN restaurant_tables t ON o.table_id = t.id "
                + "JOIN users u ON o.user_id = u.id "
                + "ORDER BY o.created_at DESC";
                
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapOrder(rs));
        } catch (SQLException e) {
            System.err.println("getAllOrders error: " + e.getMessage());
        }
        return list;
    }

    /** 
     * Extracts all individual items nested under a specific parent Order. 
     */
    public static List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        // INNER JOIN fetches the 'name' string from the menu_items master table
        String sql = "SELECT oi.id, oi.order_id, oi.menu_item_id, m.name AS item_name, "
                + "oi.quantity, oi.unit_price "
                + "FROM order_items oi JOIN menu_items m ON oi.menu_item_id = m.id "
                + "WHERE oi.order_id = ?";
                
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setMenuItemId(rs.getInt("menu_item_id"));
                    oi.setMenuItemName(rs.getString("item_name"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getDouble("unit_price"));
                    list.add(oi);
                }
            }
        } catch (SQLException e) {
            System.err.println("getOrderItems error: " + e.getMessage());
        }
        return list;
    }

    /** 
     * Updates an order status to 'BILLED' signifying completion, while synchronously
     * restoring the referenced physical table layout to an available 'FREE' state.
     */
    public static boolean markOrderBilled(int orderId, int tableId) {
        String sql = "UPDATE orders SET status='BILLED' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                // Relinquish table footprint if exactly one order was billed 
                updateTableStatus(tableId, RestaurantTable.Status.FREE);
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("markOrderBilled error: " + e.getMessage());
            return false;
        }
    }

    // ── Private helpers ──────────────────────────────────────────

    /** Utility to extract a fully fleshed out Order model from a DB cursor */
    private static Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setTableId(rs.getInt("table_id"));
        o.setTableNumber(rs.getInt("table_number"));
        o.setUserId(rs.getInt("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setStatus(Order.Status.valueOf(rs.getString("status")));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
