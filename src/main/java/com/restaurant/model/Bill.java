package com.restaurant.model;

import java.sql.Timestamp;

/**
 * Model class representing a generated final bill (invoice) for a completed order.
 * Maps to the 'bills' table in the database and explicitly stores tax and discount breakdowns.
 */
public class Bill {
    // Unique identifier for the bill
    private int id;
    
    // The Order this bill finalizes (1:1 relationship)
    private int orderId;
    
    // Customer details
    private String customerName;
    private String customerPhone;
    
    // Convenience field displaying the table number for reference
    private int tableNumber;
    
    // Base cost before any taxes or discounts are applied
    private double subtotal;
    
    // Applied discount percentage
    private double discountPct;
    
    // Explicitly stored monetary discount amount
    private double discountAmt;
    
    // Applicable tax percentage
    private double taxPct;
    
    // Explicitly stored monetary tax amount
    private double taxAmt;
    
    // Grand total to be paid by the customer
    private double totalAmount;
    
    // Automatically recorded timestamp when the bill was generated
    private Timestamp createdAt;

    /** Default constructor */
    public Bill() {
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int o) {
        this.orderId = o;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int n) {
        this.tableNumber = n;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double s) {
        this.subtotal = s;
    }

    public double getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(double d) {
        this.discountPct = d;
    }

    public double getDiscountAmt() {
        return discountAmt;
    }

    public void setDiscountAmt(double d) {
        this.discountAmt = d;
    }

    public double getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(double t) {
        this.taxPct = t;
    }

    public double getTaxAmt() {
        return taxAmt;
    }

    public void setTaxAmt(double t) {
        this.taxAmt = t;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double t) {
        this.totalAmount = t;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp t) {
        this.createdAt = t;
    }
}
