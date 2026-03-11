# 🍽 Restaurant Management System

A complete, college-level Restaurant Management System built with **Java Swing** (GUI) and **MySQL** (backend), following **MVC architecture**.

---

## 📋 Features

| Module | Description |
|---|---|
| **Login** | Role-based login — Admin and Staff |
| **Table Layout** | Visual grid of tables with FREE / OCCUPIED status |
| **Order Management** | Browse menu by category, add/remove items, real-time total |
| **Bill Generation** | Itemised receipt with discount, 5% GST, print support |
| **Admin – Menu** | Add / Edit / Delete menu items |
| **Admin – Staff** | Manage staff accounts |
| **Admin – Reports** | Daily & monthly sales revenue reports |
| **Admin – Bills** | Full bill history with total revenue |

---

## 🛠 Prerequisites

| Tool | Version |
|---|---|
| JDK | 17 or higher |
| MySQL Server | 8.0 or higher |
| MySQL Connector/J | Included in `lib/mysql-connector-j-8.3.0.jar` |

---

## ⚙️ Database Setup

1. Start MySQL and open a terminal / MySQL Workbench.
2. Run the schema script:

```bash
mysql -u root -p < sql/schema.sql
```

This creates the `restaurant_db` database with all tables and seeds:
- 4 categories, 21 menu items
- 3 user accounts (see below)
- 10 restaurant tables

### Default Login Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `staff` | `staff123` | Staff |
| `waiter1` | `waiter123` | Staff |

---

## 🚀 Running the Application

### Step 1 — Configure DB credentials (if needed)

Edit `src/main/java/com/restaurant/db/DBConnection.java`:
```java
private static final String USER     = "root";     // your MySQL username
private static final String PASSWORD = "root";     // your MySQL password
```

### Step 2 — Compile

From the project root:

**macOS / Linux:**
```bash
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
```

**Windows:**
```cmd
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
```

### Step 3 — Run

**macOS / Linux:**
```bash
java -cp "out:lib/*" com.restaurant.App
```

**Windows:**
```cmd
java -cp "out;lib/*" com.restaurant.App
```

---

## 📁 Project Structure

```
Restaurant-management-service/
├── lib/
│   └── mysql-connector-j-8.3.0.jar
├── sql/
│   └── schema.sql              ← Full DB schema + seed data
├── src/main/java/com/restaurant/
│   ├── App.java                ← Entry point
│   ├── db/
│   │   └── DBConnection.java   ← JDBC singleton
│   ├── model/                  ← POJOs (User, MenuItem, Order, Bill…)
│   ├── controller/             ← Business logic (Auth, Menu, Order, Bill, Admin, Report)
│   └── view/                   ← Swing GUI (Login, Dashboard, Table, Order, Bill dialogs…)
└── README.md
```

---

## 🏗 Architecture

This project follows the **MVC (Model–View–Controller)** pattern:

- **Model** — Plain Java POJOs representing database entities
- **Controller** — Contains all database queries using `PreparedStatement` (SQL injection safe)
- **View** — Java Swing panels; no business logic

---

## 📊 Database Schema

```
users ──────────────── orders ─────────── order_items ── menu_items
                          │                                    │
restaurant_tables ────────┘              categories ──────────┘
                          │
                        bills
```

---

## 🎨 UI Theme

- **Color palette**: Deep Red `#8B1A1A`, Warm Orange `#E87722`, Cream `#FFF8F0`
- **Sidebar**: Dark brown with hover effects
- **Buttons**: Rounded, hover-animated
- **Tables**: Custom header styled with restaurant colours

---

## 📝 Project Report Notes

### Problem Statement
Traditional pen-and-paper restaurant management is error-prone and slow. This system digitises the complete order-to-bill workflow with role-based access control.

### Algorithm — Order to Bill Flow
```
1. Staff selects a FREE table → order created in DB, table marked OCCUPIED
2. Staff browses menu categories → adds items to cart (stored in order_items)
3. Real-time total displayed as items are added
4. Staff clicks "Generate Bill" → enters discount %
5. System calculates: subtotal → discount → GST (5%) → total
6. Bill saved to `bills` table, order marked BILLED, table marked FREE
7. Receipt dialog shown with print option
```

### ER Diagram (Text Description)
- `users` (1) ──< `orders` (N) — one staff member creates many orders
- `restaurant_tables` (1) ──< `orders` (N) — one table has many orders over time
- `orders` (1) ──< `order_items` (N) — one order has many line items
- `menu_items` (1) ──< `order_items` (N) — one item appears in many orders
- `categories` (1) ──< `menu_items` (N) — one category has many items
- `orders` (1) ── `bills` (1) — one order has at most one bill

### Limitations
- Passwords stored as plain text (production would use bcrypt)
- Single-machine JDBC connection (not pooled)
- No network/multi-user concurrent access handling

### Future Scope
- Password hashing (BCrypt)
- Online ordering / QR code menu
- Kitchen display system (KDS)
- SMS / email bill delivery
- Cloud database deployment
