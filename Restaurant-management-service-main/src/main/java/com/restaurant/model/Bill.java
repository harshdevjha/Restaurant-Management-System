package com.restaurant.model;

import java.sql.Timestamp;

/**
 * Model representing a generated bill for an order.
 */
public class Bill {
    private int id;
    private int orderId;
    private int tableNumber; // convenience
    private double subtotal;
    private double discountPct;
    private double discountAmt;
    private double taxPct;
    private double taxAmt;
    private double totalAmount;
    private Timestamp createdAt;

    public Bill() {
    }

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
