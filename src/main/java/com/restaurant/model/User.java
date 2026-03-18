package com.restaurant.model;

/**
 * Model class representing a system user (e.g., Admin or Staff).
 * This class maps to the 'users' table in the database and is used 
 * to store user credentials and authorization roles across the application.
 */
public class User {
    // Unique identifier for the user (Primary Key in DB)
    private int id;
    
    // Login username
    private String username;
    
    // Login password (currently stored as plain text per schema)
    private String password;
    
    // Display name of the user
    private String fullName;
    
    // Role for authorization purposes (Expected values: "ADMIN" or "STAFF")
    private String role; 
    
    // Status flag to indicate if the user account is active
    private boolean active;

    /**
     * Default constructor required for certain framework instantiations 
     * or manual bean creation.
     */
    public User() {
    }

    /**
     * Parameterized constructor used when fetching user data from the database.
     */
    public User(int id, String username, String password, String fullName, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    
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

    /**
     * Utility method to check if this user has administrator privileges.
     * @return true if the role is exactly "ADMIN", false otherwise.
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Overrides default toString to provide a useful representation of the User,
     * frequently used when binding User objects to UI dropdowns or lists.
     */
    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
