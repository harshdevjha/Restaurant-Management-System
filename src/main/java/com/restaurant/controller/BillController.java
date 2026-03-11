package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Bill;
import com.restaurant.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BillController – generates bills with discount and tax calculations,
 * persists them to the database, and retrieves bill history.
 */
public class BillController {

    /**
     * Generates, saves, and returns a Bill for the given order.
     *
     * @param orderId     the order to bill
     * @param tableId     the table (to mark FREE after billing)
     * @param discountPct percentage discount (0-100)
     * @param taxPct      GST/Tax percentage (e.g. 5.0 for 5%)
     * @return the persisted Bill object, or null on failure
     */
    public static Bill generateBill(int orderId, int tableId,
            double discountPct, double taxPct) {
        // 1. Fetch order items to calculate subtotal
        List<OrderItem> items = OrderController.getOrderItems(orderId);
        if (items.isEmpty())
            return null;

        double subtotal = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double discountAmt = subtotal * (discountPct / 100.0);
        double afterDisc = subtotal - discountAmt;
        double taxAmt = afterDisc * (taxPct / 100.0);
        double total = afterDisc + taxAmt;

        // 2. Persist bill
        String sql = "INSERT INTO bills (order_id, subtotal, discount_pct, discount_amt, "
                + "tax_pct, tax_amt, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setDouble(2, subtotal);
            ps.setDouble(3, discountPct);
            ps.setDouble(4, discountAmt);
            ps.setDouble(5, taxPct);
            ps.setDouble(6, taxAmt);
            ps.setDouble(7, total);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Bill bill = new Bill();
                    bill.setId(rs.getInt(1));
                    bill.setOrderId(orderId);
                    bill.setSubtotal(subtotal);
                    bill.setDiscountPct(discountPct);
                    bill.setDiscountAmt(discountAmt);
                    bill.setTaxPct(taxPct);
                    bill.setTaxAmt(taxAmt);
                    bill.setTotalAmount(total);

                    // 3. Mark order as BILLED and free the table
                    OrderController.markOrderBilled(orderId, tableId);
                    return bill;
                }
            }
        } catch (SQLException e) {
            System.err.println("generateBill error: " + e.getMessage());
        }
        return null;
    }

    /** Returns all bills, most recent first. */
    public static List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.id, b.order_id, t.number AS table_number, "
                + "b.subtotal, b.discount_pct, b.discount_amt, "
                + "b.tax_pct, b.tax_amt, b.total_amount, b.created_at "
                + "FROM bills b "
                + "JOIN orders o ON b.order_id = o.id "
                + "JOIN restaurant_tables t ON o.table_id = t.id "
                + "ORDER BY b.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapBill(rs));
        } catch (SQLException e) {
            System.err.println("getAllBills error: " + e.getMessage());
        }
        return list;
    }

    // ── Private helpers ──────────────────────────────────────────

    private static Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setOrderId(rs.getInt("order_id"));
        b.setTableNumber(rs.getInt("table_number"));
        b.setSubtotal(rs.getDouble("subtotal"));
        b.setDiscountPct(rs.getDouble("discount_pct"));
        b.setDiscountAmt(rs.getDouble("discount_amt"));
        b.setTaxPct(rs.getDouble("tax_pct"));
        b.setTaxAmt(rs.getDouble("tax_amt"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
