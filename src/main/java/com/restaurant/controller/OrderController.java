package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.RestaurantTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderController – manages order lifecycle and restaurant table state.
 */
public class OrderController {

    // ── Tables ───────────────────────────────────────────────────

    /** Returns all restaurant tables. */
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
     * Creates a new PENDING order for the given table, marks table as OCCUPIED.
     * 
     * @return the generated order ID, or -1 on failure
     */
    public static int createOrder(int tableId, int userId) {
        String sql = "INSERT INTO orders (table_id, user_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
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
     * Adds or increments a menu item in an existing order.
     * If the item already exists, its quantity is increased by the given amount.
     */
    public static boolean addItemToOrder(int orderId, int menuItemId, String itemName,
            double unitPrice, int quantity) {
        // Check if item already in order
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
                        int existingId = rs.getInt("id");
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, quantity);
                            upd.setInt(2, existingId);
                            return upd.executeUpdate() > 0;
                        }
                    }
                }
            }
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

    /** Removes an order item by its ID. */
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

    /** Updates the quantity of an order item. Removes it if quantity ≤ 0. */
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
     * Fetches the active (PENDING or SERVED) order for a table, including all
     * items.
     * Returns null if the table has no active order.
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
                    order.setItems(getOrderItems(order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("getActiveOrderForTable error: " + e.getMessage());
        }
        return null;
    }

    /** Returns all orders (for admin view). */
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

    /** Returns all order items for a given order. */
    public static List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
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

    /** Marks order as BILLED and frees the table. */
    public static boolean markOrderBilled(int orderId, int tableId) {
        String sql = "UPDATE orders SET status='BILLED' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok)
                updateTableStatus(tableId, RestaurantTable.Status.FREE);
            return ok;
        } catch (SQLException e) {
            System.err.println("markOrderBilled error: " + e.getMessage());
            return false;
        }
    }

    // ── Private helpers ──────────────────────────────────────────

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
