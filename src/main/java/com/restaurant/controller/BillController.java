package com.restaurant.controller;

import com.restaurant.db.DBConnection;
import com.restaurant.model.Bill;
import com.restaurant.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BillController – Executes the financial mathematics for closing an order.
 * Generates bills factoring in discounts and taxes, persists the finalized ledger 
 * to the database, and provides API access to historical bills.
 */
public class BillController {

    /**
     * Finalizes and generates an official Bill for a completed order.
     * Computes totals mathematically before locking them into the 'bills' DB table.
     *
     * @param orderId     the parent order to finalize.
     * @param tableId     the associated table (to mark FREE after successful billing).
     * @param discountPct applied discount percentage (0-100).
     * @param taxPct      applicable GST/Tax percentage (e.g. 5.0 for 5%).
     * @return the fully populated persisted Bill object, or null on failure.
     */
    public static Bill generateBill(int orderId, int tableId,
            double discountPct, double taxPct) {
            
        // 1. Fetch order items to calculate baseline subtotal
        List<OrderItem> items = OrderController.getOrderItems(orderId);
        if (items.isEmpty())
            return null; // Cancel operation if there's nothing to bill.

        // Compute subtotal algebraically mapping over every order item subtotal
        double subtotal = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        
        // Calculate the raw discount amount using the derived subtotal
        double discountAmt = subtotal * (discountPct / 100.0);
        
        // Subtract to form the post-discounted sum
        double afterDisc = subtotal - discountAmt;
        
        // Finally, append tax scaling onto the discounted subtotal
        double taxAmt = afterDisc * (taxPct / 100.0);
        
        // Total customer cash owed
        double total = afterDisc + taxAmt;

        // 2. Persist comprehensive bill to lock in these calculations permanently
        String sql = "INSERT INTO bills (order_id, subtotal, discount_pct, discount_amt, "
                + "tax_pct, tax_amt, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
                
        try (Connection conn = DBConnection.getConnection();
                // Request generated keys parameter guarantees we capture the DB-inserted Primary Key
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
            ps.setInt(1, orderId);
            ps.setDouble(2, subtotal);
            ps.setDouble(3, discountPct);
            ps.setDouble(4, discountAmt);
            ps.setDouble(5, taxPct);
            ps.setDouble(6, taxAmt);
            ps.setDouble(7, total);
            ps.executeUpdate();

            // Extract the generated primary key for the new bill
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Bill bill = new Bill();
                    bill.setId(rs.getInt(1)); // The extracted newly generated DB row ID
                    bill.setOrderId(orderId);
                    bill.setSubtotal(subtotal);
                    bill.setDiscountPct(discountPct);
                    bill.setDiscountAmt(discountAmt);
                    bill.setTaxPct(taxPct);
                    bill.setTaxAmt(taxAmt);
                    bill.setTotalAmount(total);

                    // 3. Mark the underlying order as BILLED which intrinsically frees the table
                    OrderController.markOrderBilled(orderId, tableId);
                    return bill;
                }
            }
        } catch (SQLException e) {
            System.err.println("generateBill error: " + e.getMessage());
        }
        return null;
    }

    /** 
     * Retrieves the entire ledger of all generated bills, most recent first.
     * Incorporates data from the orders and restaurant_tables relations.
     * @return List of fully mapped Bill models.
     */
    public static List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        // INNER JOIN retrieves the convenience table_number that was originally used.
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

    /** Utility abstraction for instantiating Bill models directly from a ResultSet cursor */
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
