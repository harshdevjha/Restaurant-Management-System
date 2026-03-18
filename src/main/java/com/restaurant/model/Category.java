package com.restaurant.model;

/**
 * Model class representing a menu category (e.g. Starters, Main Course).
 * Used to group menu items logically in the UI and database.
 */
public class Category {
    // Unique identifier from the categories DB table
    private int    id;
    
    // Name of the category
    private String name;
    
    // Brief description of the types of food in this category
    private String description;

    /** Provide a no-args constructor for reflection/bean instantiation */
    public Category() {}
    
    /**
     * Parameterized constructor used to map database records into Category objects.
     */
    public Category(int id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }
    
    public String getName()                  { return name; }
    public void   setName(String n)          { this.name = n; }
    
    public String getDescription()           { return description; }
    public void   setDescription(String d)   { this.description = d; }

    /**
     * Standard toString mapping, often used by UI components (like JComboBox) 
     * to display the category name naturally.
     */
    @Override public String toString()       { return name; }
}
