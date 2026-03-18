package com.restaurant.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a customer order session for a specific table.
 * Maps to the 'orders' table in the database and holds a collection of OrderItems.
 */
public class Order {
    /**
     * Enumeration defining the lifecycle of an order:
     * - PENDING: Order is taken but not yet served completely.
     * - SERVED: Food has been served to the customer.
     * - BILLED: The final bill has been generated and the order is complete.
     */
    public enum Status {
        PENDING, SERVED, BILLED
    }

    // Unique order ID (Primary Key)
    private int id;
    
    // The physical table this order belongs to
    private int tableId;
    
    // Convenience field for quick UI rendering of the table number
    private int tableNumber;
    
    // The staff member who took this order
    private int userId;
    
    // Convenience field representing the staff's name for UI display
    private String userName;
    
    // Current lifecycle status of the order
    private Status status;
    
    // When the order was first created
    private Timestamp createdAt;
    
    // List of individual items (dishes/beverages) ordered during this session
    private List<OrderItem> items = new ArrayList<>();

    /** Default constructor */
    public Order() {
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
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

    /** 
     * Utility method to calculate the total price of all items currently in the order. 
     * @return the total sum of subtotals from each order item.
     */
    public double getSubtotal() {
        double total = 0;
        for (OrderItem oi : items)
            total += oi.getSubtotal();
        return total;
    }
}
