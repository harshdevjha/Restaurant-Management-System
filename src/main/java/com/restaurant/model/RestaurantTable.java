package com.restaurant.model;

/**
 * Model representing a physical restaurant table.
 */
public class RestaurantTable {
    public enum Status { FREE, OCCUPIED }

    private int    id;
    private int    number;
    private int    capacity;
    private Status status;

    public RestaurantTable() {}

    public RestaurantTable(int id, int number, int capacity, Status status) {
        this.id = id; this.number = number;
        this.capacity = capacity; this.status = status;
    }

    public int    getId()                  { return id; }
    public void   setId(int id)           { this.id = id; }
    public int    getNumber()              { return number; }
    public void   setNumber(int n)        { this.number = n; }
    public int    getCapacity()            { return capacity; }
    public void   setCapacity(int c)      { this.capacity = c; }
    public Status getStatus()              { return status; }
    public void   setStatus(Status s)     { this.status = s; }

    public boolean isFree()               { return status == Status.FREE; }

    @Override public String toString()    { return "Table " + number; }
}
