-- ================================================================
--  Restaurant Management System - Database Schema & Seed Data
--  Database: restaurant_db
--  Author:   Restaurant Management System
--  Version:  1.0
-- ================================================================

CREATE DATABASE IF NOT EXISTS restaurant_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- ----------------------------------------------------------------
-- Table: categories
-- Purpose: Stores the broad categories for menu items (e.g., Starters, Main Course).
-- Relations: Referenced by menu_items (1:N relationship).
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: users  (admin & staff accounts)
-- Purpose: Manages system users, authentication credentials, and their roles for authorization.
-- Relations: Referenced by orders to track which staff member handled them (1:N relationship).
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,          -- stored as plain text for demo
    full_name  VARCHAR(100) NOT NULL,
    role       ENUM('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF',
    active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: menu_items
-- Purpose: Defines the dishes and beverages available, along with prices and availability status.
-- Relations: Belongs to a category. Referenced by order_items.
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS menu_items (
    id          INT            AUTO_INCREMENT PRIMARY KEY,
    category_id INT            NOT NULL,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(255),
    price       DECIMAL(10,2)  NOT NULL,
    available   TINYINT(1)     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_item_category (category_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: restaurant_tables
-- Purpose: Tracks physical table attributes, capacity, and current occupancy status.
-- Relations: Referenced by orders (1:N relationship).
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurant_tables (
    id       INT         AUTO_INCREMENT PRIMARY KEY,
    number   INT         NOT NULL UNIQUE,
    capacity INT         NOT NULL DEFAULT 4,
    status   ENUM('FREE','OCCUPIED') NOT NULL DEFAULT 'FREE'
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: orders
-- Purpose: Represents a customer's visit/order session at a specific table, managed by a user.
-- Relations: Belongs to user and restaurant_table. Has many order_items. Has one bill.
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id         INT       AUTO_INCREMENT PRIMARY KEY,
    table_id   INT       NOT NULL,
    user_id    INT       NOT NULL,
    status     ENUM('PENDING','SERVED','BILLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id),
    CONSTRAINT fk_order_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    INDEX idx_order_table  (table_id),
    INDEX idx_order_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: order_items
-- Purpose: The line items for An order. Connects an order to specific menu items.
-- Relations: Links orders and menu_items (Many-to-Many resolution table).
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id           INT           AUTO_INCREMENT PRIMARY KEY,
    order_id     INT           NOT NULL,
    menu_item_id INT           NOT NULL,
    quantity     INT           NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_oi_order     FOREIGN KEY (order_id)     REFERENCES orders(id)     ON DELETE CASCADE,
    CONSTRAINT fk_oi_menuitem  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    INDEX idx_oi_order (order_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------
-- Table: bills
-- Purpose: The final invoice generated from an order, storing tax, discount, and total amounts.
-- Relations: Belongs to a single order (1:1 relationship).
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    id            INT           AUTO_INCREMENT PRIMARY KEY,
    order_id      INT           NOT NULL UNIQUE,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    subtotal      DECIMAL(10,2) NOT NULL,
    discount_pct  DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    discount_amt  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_pct       DECIMAL(5,2)  NOT NULL DEFAULT 5.00,
    tax_amt       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount  DECIMAL(10,2) NOT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_bill_date (created_at)
) ENGINE=InnoDB;

-- ================================================================
--  SEED DATA
-- ================================================================

-- Categories
INSERT INTO categories (name, description) VALUES
    ('Starters',    'Appetizers and small bites'),
    ('Main Course', 'Full meals and main dishes'),
    ('Beverages',   'Hot and cold drinks'),
    ('Desserts',    'Sweet treats and desserts');

-- Users  (password: admin123 / staff123)
INSERT INTO users (username, password, full_name, role) VALUES
    ('admin', 'admin123', 'Admin User',    'ADMIN'),
    ('staff', 'staff123', 'Staff Member',  'STAFF'),
    ('waiter1', 'waiter123', 'Raj Kumar',  'STAFF');

-- Menu Items — Starters (category_id = 1)
INSERT INTO menu_items (category_id, name, description, price) VALUES
    (1, 'Veg Spring Rolls',     'Crispy rolls stuffed with mixed vegetables',       120.00),
    (1, 'Chicken Tikka',        'Tender chicken marinated in spices and grilled',   220.00),
    (1, 'Paneer Tikka',         'Cottage cheese cubes grilled with peppers',        180.00),
    (1, 'Mushroom Soup',        'Creamy mushroom soup with garlic bread',           130.00),
    (1, 'Crispy Calamari',      'Lightly battered squid rings with dipping sauce',  250.00);

-- Menu Items — Main Course (category_id = 2)
INSERT INTO menu_items (category_id, name, description, price) VALUES
    (2, 'Butter Chicken',       'Classic creamy tomato curry with naan',            320.00),
    (2, 'Dal Makhani',          'Slow-cooked black lentils in rich gravy',          240.00),
    (2, 'Biryani (Chicken)',    'Aromatic basmati rice with spiced chicken',        350.00),
    (2, 'Biryani (Veg)',        'Aromatic basmati rice with seasonal vegetables',   280.00),
    (2, 'Grilled Fish Fillet',  'Fresh fish fillet with herb butter and fries',     380.00),
    (2, 'Pasta Arrabiata',      'Penne in spicy tomato sauce with herbs',           260.00),
    (2, 'Paneer Butter Masala', 'Cottage cheese in rich buttery tomato gravy',      290.00);

-- Menu Items — Beverages (category_id = 3)
INSERT INTO menu_items (category_id, name, description, price) VALUES
    (3, 'Mango Lassi',          'Chilled blended yogurt with fresh mango',           80.00),
    (3, 'Fresh Lime Soda',      'Refreshing lime with soda water',                   60.00),
    (3, 'Masala Chai',          'Spiced Indian milk tea',                            40.00),
    (3, 'Cold Coffee',          'Blended cold coffee with ice cream',                110.00),
    (3, 'Fresh Juice',          'Seasonal fresh fruit juice',                        90.00);

-- Menu Items — Desserts (category_id = 4)
INSERT INTO menu_items (category_id, name, description, price) VALUES
    (4, 'Gulab Jamun',          'Soft milk solids dumplings soaked in sugar syrup',  80.00),
    (4, 'Chocolate Brownie',    'Warm chocolate brownie with vanilla ice cream',     150.00),
    (4, 'Rasmalai',             'Soft cottage cheese patties in creamy milk',        100.00),
    (4, 'Ice Cream (2 Scoops)', 'Choice of vanilla, chocolate or strawberry',        120.00);

-- Restaurant Tables (10 tables)
INSERT INTO restaurant_tables (number, capacity) VALUES
    (1, 2), (2, 2), (3, 4), (4, 4), (5, 4),
    (6, 4), (7, 6), (8, 6), (9, 8), (10, 10);
