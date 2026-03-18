package com.restaurant.controller;

import com.restaurant.db.DBConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ReportController – Aggregation utility that interrogates the 'bills' DB table 
 * to generate sales metrics and operational revenue reports across periods.
 */
public class ReportController {

    /**
     * Executes an aggregation query determining sales figures for a distinct daily calendar window.
     * Computes total bill generation volume paired with summed monetary intake.
     *
     * @param date The exact calendar date evaluating the period of search.
     * @return A linked map populated containing attributes: "date", "bill_count", "revenue".
     */
    public static Map<String, Object> getDailyReport(java.util.Date date) {
        // LinkedHashMap chosen to guarantee preservation of insertion order during retrieval
        Map<String, Object> result = new LinkedHashMap<>();
        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);

        // Uses MySQL's DATE() function to strip times off of the DB timestamp for comparison
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
     * Retrieves aggregated performance figures structured as a time series, 
     * broken down row-by-row for each day matching the defined calendar month/year.
     *
     * @param month The numerical month designation (1-12)
     * @param year The numerical explicit 4-digit year designation (e.g. 2026)
     * @return A sequential List containing mapping instances outlining sales totals per actual date.
     */
    public static java.util.List<Map<String, Object>> getMonthlyReport(int month, int year) {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        
        // This query utilizes a GROUP BY aggregation function combined with intrinsic MySQL DATE/MONTH functions. 
        // Generates sub-totals isolating each individual active calendar day.
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

    /** 
     * Yields a single absolute revenue accumulation summation resolving an entire month's worth of completed operations.
     * @param month Specified mathematical calendar month (1-12).
     * @param year Specified bounding calendar numeric year.
     * @return Double precision arithmetic representing total amassed revenue.
     */
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
        return 0; // Fallback safely to zero during DB connectivity or arithmetic errors.
    }
}
