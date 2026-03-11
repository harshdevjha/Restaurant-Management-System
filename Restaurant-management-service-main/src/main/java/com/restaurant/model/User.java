package com.restaurant.model;

/**
 * Model representing a system user (Admin or Staff).
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role; // "ADMIN" or "STAFF"
    private boolean active;

    public User() {
    }

    public User(int id, String username, String password, String fullName, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String n) {
        this.fullName = n;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String r) {
        this.role = r;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean a) {
        this.active = a;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
