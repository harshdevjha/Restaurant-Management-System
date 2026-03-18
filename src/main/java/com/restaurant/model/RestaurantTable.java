package com.restaurant.model;

/**
 * Model class representing a physical restaurant table.
 * Corresponds to the 'restaurant_tables' table in the database schema.
 */
public class RestaurantTable {
    
    /**
     * Enumeration defining the current occupancy status of the table.
     */
    public enum Status { FREE, OCCUPIED }

    // Unique table ID from DB
    private int    id;
    
    // Physical table number assigned in the restaurant
    private int    number;
    
    // Number of people the table can comfortably seat
    private int    capacity;
    
    // Current availability of the table
    private Status status;

    /**
     * Default constructor for bean instantiation.
     */
    public RestaurantTable() {}

    /**
     * Parameterized constructor used when initializing table objects from DB result sets.
     */
    public RestaurantTable(int id, int number, int capacity, Status status) {
        this.id = id; this.number = number;
        this.capacity = capacity; this.status = status;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }
    
    public int    getNumber()               { return number; }
    public void   setNumber(int n)          { this.number = n; }
    
    public int    getCapacity()             { return capacity; }
    public void   setCapacity(int c)        { this.capacity = c; }
    
    public Status getStatus()               { return status; }
    public void   setStatus(Status s)       { this.status = s; }

    /**
     * Helper method to quickly determine if the table is available for seating.
     * @return true if status is FREE, false otherwise.
     */
    public boolean isFree()                 { return status == Status.FREE; }

    /**
     * Returns a human-readable representation, handy for displaying in dropdowns or lists.
     */
    @Override public String toString()      { return "Table " + number; }
}
