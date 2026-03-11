package com.restaurant.model;

/**
 * Model representing a single menu item.
 */
public class MenuItem {
    private int id;
    private int categoryId;
    private String categoryName; // convenience field
    private String name;
    private String description;
    private double price;
    private boolean available;

    public MenuItem() {
    }

    public MenuItem(int id, int categoryId, String name, String description,
            double price, boolean available) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

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

    @Override
    public String toString() {
        return name + " - ₹" + price;
    }
}
