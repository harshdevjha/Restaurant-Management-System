package com.restaurant.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a customer order for a table.
 */
public class Order {
    public enum Status {
        PENDING, SERVED, BILLED
    }

    private int id;
    private int tableId;
    private int tableNumber; // convenience
    private int userId;
    private String userName; // convenience
    private Status status;
    private Timestamp createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int t) {
        this.tableId = t;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int n) {
        this.tableNumber = n;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int u) {
        this.userId = u;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String n) {
        this.userName = n;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status s) {
        this.status = s;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp t) {
        this.createdAt = t;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> l) {
        this.items = l;
    }

    /** Returns the sum of all order item subtotals. */
    public double getSubtotal() {
        double total = 0;
        for (OrderItem oi : items)
            total += oi.getSubtotal();
        return total;
    }
}
