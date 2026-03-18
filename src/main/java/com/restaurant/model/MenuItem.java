package com.restaurant.model;

/**
 * Model class representing a single menu item (e.g., a specific dish or drink).
 * Maps to the 'menu_items' table in the database.
 */
public class MenuItem {
    // Unique identifier for the menu item
    private int id;
    
    // Foreign key linking this item to a specific Category
    private int categoryId;
    
    // Convenience field to store the category's name for UI display without joining tables again
    private String categoryName; 
    
    // Name of the dish/item
    private String name;
    
    // Detailed description of the ingredients/preparation
    private String description;
    
    // Cost of the item
    private double price;
    
    // Flag to temporarily disable ordering this item if it is out of stock
    private boolean available;

    /** Default constructor for bean instantiation */
    public MenuItem() {
    }

    /**
     * Parameterized constructor handling full initialization from database records.
     */
    public MenuItem(int id, int categoryId, String name, String description,
            double price, boolean available) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int c) {
        this.categoryId = c;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String n) {
        this.categoryName = n;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double p) {
        this.price = p;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean a) {
        this.available = a;
    }

    /**
     * Standard toString mapping, generally used by JList or JComboBox in the UI
     * to show the item name alongside its price.
     */
    @Override
    public String toString() {
        return name + " - ₹" + price;
    }
}
