package com.restaurant.controller;

import com.restaurant.db.DBConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ReportController – generates sales revenue reports by day or month.
 */
public class ReportController {

    /**
     * Returns daily sales: a map of "YYYY-MM-DD" → total revenue for the given
     * date.
     * Also includes itemised breakdown via columns in the returned map.
     */
    public static Map<String, Object> getDailyReport(java.util.Date date) {
        Map<String, Object> result = new LinkedHashMap<>();
        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);

        String sql = "SELECT COUNT(b.id) AS bill_count, SUM(b.total_amount) AS revenue "
                + "FROM bills b WHERE DATE(b.created_at) = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("date", dateStr);
                    result.put("bill_count", rs.getInt("bill_count"));
                    result.put("revenue", rs.getDouble("revenue"));
                }
            }
        } catch (SQLException e) {
            System.err.println("getDailyReport error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns a per-day breakdown for the specified month/year.
     * Each entry: "YYYY-MM-DD" → { date, bill_count, revenue }
     */
    public static java.util.List<Map<String, Object>> getMonthlyReport(int month, int year) {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        String sql = "SELECT DATE(b.created_at) AS sale_date, "
                + "COUNT(b.id) AS bill_count, SUM(b.total_amount) AS revenue "
                + "FROM bills b "
                + "WHERE MONTH(b.created_at) = ? AND YEAR(b.created_at) = ? "
                + "GROUP BY DATE(b.created_at) ORDER BY sale_date";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", rs.getString("sale_date"));
                    row.put("bill_count", rs.getInt("bill_count"));
                    row.put("revenue", rs.getDouble("revenue"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("getMonthlyReport error: " + e.getMessage());
        }
        return list;
    }

    /** Returns total revenue for a given month/year. */
    public static double getTotalMonthlyRevenue(int month, int year) {
        String sql = "SELECT SUM(total_amount) AS total FROM bills "
                + "WHERE MONTH(created_at)=? AND YEAR(created_at)=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("getTotalMonthlyRevenue error: " + e.getMessage());
        }
        return 0;
    }
}
