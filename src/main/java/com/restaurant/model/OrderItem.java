package com.restaurant.model;

/**
 * Model representing a single line item within an order.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String menuItemName; // convenience
    private int quantity;
    private double unitPrice;

    public OrderItem() {
    }

    public OrderItem(int menuItemId, String name, int quantity, double unitPrice) {
        this.menuItemId = menuItemId;
        this.menuItemName = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int m) {
        this.menuItemId = m;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String n) {
        this.menuItemName = n;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double p) {
        this.unitPrice = p;
    }

    /** Returns quantity × unit price. */
    public double getSubtotal() {
        return quantity * unitPrice;
    }
}
