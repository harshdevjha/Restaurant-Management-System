package com.restaurant.model;

/**
 * Model class representing a single line item within an order.
 * Acts as a junction entity mapping a specific MenuItem to an Order, along with quantity and price.
 */
public class OrderItem {
    // Unique ID for this specific line item record in the DB
    private int id;
    
    // The Order session this item belongs to
    private int orderId;
    
    // The actual dish/beverage ordered
    private int menuItemId;
    
    // Convenience field for displaying the item's name without querying the DB
    private String menuItemName; 
    
    // Number of portions ordered
    private int quantity;
    
    // Price per single portion explicitly stored to prevent historical changes
    // if the MenuItem's master price is updated later.
    private double unitPrice;

    /** Default constructor */
    public OrderItem() {
    }

    /**
     * Parameterized constructor generally used when building new items before saving to DB.
     */
    public OrderItem(int menuItemId, String name, int quantity, double unitPrice) {
        this.menuItemId = menuItemId;
        this.menuItemName = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    /** 
     * Calculates the total cost for this specific line item.
     * @return quantity × unit price. 
     */
    public double getSubtotal() {
        return quantity * unitPrice;
    }
}
