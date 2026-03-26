# 🍽 Restaurant Management System (RMS)

A premium, college-level Restaurant Management System built with **Java Swing** and **MySQL**, adhering to the **MVC (Model-View-Controller)** architecture.

---

## ⚡ Quick Start

### 1. Database Setup
```bash
mysql -u root -p < sql/schema.sql
```

### 2. Configure Credentials
Update `src/main/java/com/restaurant/db/DBConnection.java` with your MySQL user/password. (Default: `root` / ``)

### 3. Build & Run
```bash
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
java -cp "out:lib/*" com.restaurant.App
```

---

## 📋 Core Modules

| Module | Functionality |
| :--- | :--- |
| **Auth** | Role-based login for **Admin** and **Staff**. |
| **Dashboard** | Visual table grid with live status (**FREE / OCCUPIED**). |
| **Orders** | Category-based menu browsing, real-time cart, and order tracking. |
| **Billing** | **New:** Captures Customer Name/Phone. Supports discounts & 5% GST. |
| **Admin** | Full CRUD for Menu/Staff and comprehensive Sales Reports. |

---

## 🚀 Default Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Staff** | `staff` | `staff123` |

---

## 🏗 Architecture & Tech

- **Language**: Java 17+
- **Database**: MySQL 8.0+
- **GUI**: Java Swing
- **Pattern**: MVC (Separate Data, Logic, and UI)
- **Styling**: Custom `UITheme` using Deep Red (`#8B1A1A`) and Warm Orange (`#E87722`).

---

## 📁 Project Structure

```text
Restaurant-Management-System/
├── lib/          # JDBC Connector
├── sql/          # Database Schema & Seed Data
├── src/          # Source Code
│   ├── model/    # POJOs (Bill, Order, MenuItem...)
│   ├── view/     # Swing GUI Components
│   └── controller/# Database Logic & Business Rules
└── out/          # Compiled classes
```

---

## 📝 Key Design Considerations

- **Customer Data**: Bills now record customer identity for better CRM.
- **MVC Separation**: Controllers handle all SQL using `PreparedStatement` to prevent SQL injection.
- **UI Feel**: Rounded buttons, hover animations, and a stylized thermal-printer receipt.

---

## 🛠 Future Enhancements

- [ ] Password Hashing (BCrypt)
- [ ] Online QR-based Ordering
- [ ] Kitchen Display System (KDS)
- [ ] Email/SMS Receipt Delivery
