# Comprehensive Guide: Restaurant Management System

This document is a complete, beginner-friendly guide to understanding the Restaurant Management System. Whether you are presenting this project to a professor, interviewer, or a team member, reading this guide will give you the complete theoretical and technical knowledge to explain exactly how it works behind the scenes.

---

## 1. Project Overview & Architecture

At its core, this project is a **Desktop Application** built using **Java Swing** (for the graphical user interface) and **MySQL** (for the database).

### The MVC Architecture

The project strictly follows the **MVC (Model-View-Controller)** design pattern. This is a standard software engineering concept that divides the application into three interconnected parts:

1. **Model (Data Layer)**: These are plain Java classes (like `User`, `Order`, `MenuItem`). They represent the real-world objects in our restaurant. They don't know anything about the UI or how buttons look; they just hold data.
2. **View (UI Layer)**: This is what the user sees and interacts with. It includes screens like `LoginFrame`, `OrderPanel`, and `TableLayoutPanel`. The View only handles displaying information and registering clicks; it does not process complex business logic.
3. **Controller (Logic Layer)**: The brain of the application. Classes like `OrderController` and `BillController` act as the bridge between the View and the Model/Database. When a user clicks "Generate Bill" in the View, the View tells the Controller. The Controller calculates the math, saves it to the database, and creates a `Bill` Model to give back to the View.

---

## 2. Database Schema (The Foundation)

Before any code runs, we need a place to store data. We use a relational **MySQL Database**. The application uses the `com.restaurant.db.DBConnection` class to establish a secure link to MySQL using the JDBC driver.

The database consists of the following key tables:

- **`users`**: Stores staff members and admins. Includes role-based access (`ADMIN` vs `STAFF`) and credentials.
- **`restaurant_tables`**: Represents the physical tables (Table 1, Table 2). Tracks `capacity` and `status` (`FREE` or `OCCUPIED`).
- **`categories` & `menu_items`**: `categories` groups food (e.g., "Beverages", "Main Course"), while `menu_items` holds specific dishes, prices, and links back to their parent category.
- **`orders` & `order_items`**: `orders` tracks a user's session at a table. `order_items` tracks the specific dishes inside that order (e.g., 2x Cold Coffee).
- **`bills`**: Stores the final financial transaction, including subtotal, taxes, discounts, and final amount.

---

## 3. Step-by-Step System Workflows

Let's walk through the project exactly as a user experiences it, explaining the code that powers each step.

### Step A: Application Startup

1. **File:** `App.java`
2. **Action:** The application launches. The `main()` method executes.
3. **Logic:** It sets up the visual UI Theme (colors, fonts) using `UITheme.java`. It then attempts to connect to the database. If successful, it launches the `LoginFrame`.

### Step B: User Authentication (Login)

1. **View:** `LoginFrame.java`
2. **Controller:** `AuthController.java`
3. **Workflow:**
   - The user types their username and password and clicks Login.
   - `LoginFrame` calls `AuthController.login(username, password)`.
   - The Controller queries the `users` database table to find a matching record.
   - If a match is found, it returns a `User` object.
   - `LoginFrame` closes, and `DashboardFrame` opens, passing the logged-in user's details. The Dashboard uses the user's role (`ADMIN` or `STAFF`) to determine which tabs to show (e.g., Staff only see tables, Admins see reports).

### Step C: Table Layout

1. **View:** `TableLayoutPanel.java`
2. **Controller:** `OrderController.java`
3. **Workflow:**
   - The system needs to draw the restaurant floor plan. It calls `OrderController.getAllTables()`, which queries `restaurant_tables`.
   - The UI loops through these tables. If `status` is `FREE`, it draws a green card. If `OCCUPIED`, it draws a red card.
   - **Manual Override:** We added a "Mark Free / Form Occupied" toggle button. Clicking this triggers `OrderController.updateTableStatus()`, which instantly updates the MySQL table and repaints the UI.

### Step D: Placing an Order (The Core Feature)

This is the most complex and important part of the application.

1. **View:** `OrderPanel.java`
2. **Theory behind the UI:** The screen is split (`JSplitPane`). The left side dynamically loads tabs based on menu `categories`. Inside each tab, it loops through `menu_items`.
3. **Dynamic Images:** To make the UI look premium, we use a `SwingWorker` (a background thread) to fetch random food images from an external API (`loremflickr.com`). We use background threads so the UI doesn't freeze while waiting for the internet to download an image.
4. **Logic:**
   - When a user clicks a table, `OrderPanel` checks if an active order exists (`OrderController.getActiveOrderForTable()`).
   - If no order exists, it creates one `OrderController.createOrder()`, assigns it a `PENDING` status, and marks the hardware table `OCCUPIED` in the database.
   - **Adding Food:** Clicking "+ Add" calls `OrderController.addItemToOrder()`. It checks if the item is already in the cart. If yes, it runs an SQL `UPDATE` to increment the quantity (+1). If no, it runs an SQL `INSERT` to add a new row to `order_items`.
   - After updating the database, the cart panel refreshes to calculate the new UI totals.

### Step E: Generating the Bill & Checkout

1. **View:** `OrderPanel.java` & `BillDialog.java`
2. **Controller:** `BillController.java`
3. **Workflow:**
   - The waiter clicks "Generate Bill". A popup asks if there is a discount percentage.
   - `BillController.generateBill()` takes over. It sums up all `order_items` to get a `subtotal`.
   - **The Math Engine:** It applies the discount (`discountAmt = subtotal * discountPct / 100`). It calculates tax based on the discounted price (`taxAmt = afterDisc * taxPct / 100`). Finally, it combines them for the `total`.
   - **Persisting Data:** It executes an `INSERT INTO bills` SQL statement.
   - **Table Checkout:** Crucially, it changes the order status from `PENDING` to `BILLED`. Finally, `OrderController.markOrderBilled()` updates the `restaurant_tables` status back to `FREE` (meaning the customer has checked out and the table is ready for the next guest).
   - A `BillDialog` pops up showing a beautifully formatted printed receipt.

### Step F: Admin Features (Reports & Management)

If the logged-in user is an `ADMIN`, they get extra tabs on the Dashboard:

1. **Menu Management (`AdminMenuPanel` / `MenuController`)**: Allows the admin to add, edit, or delete dishes. Uses basic CRUD operations (Create, Read, Update, Delete) against the `menu_items` table.
2. **Staff Management (`StaffPanel`)**: Allows admins to create new Waiter accounts.
3. **Reports (`ReportsPanel` / `ReportController`)**: Generates analytics. It runs complex SQL aggregation queries (like `SUM()` and `GROUP BY`) to show how much revenue was earned on a specific day or month.

---

## 4. Key Java Concepts Used

When explaining this project, highlighting these specific Java features will show deep understanding:

- **JDBC (Java Database Connectivity)**: The native Java API used to connect to MySQL. We specifically use `PreparedStatement` interfaces instead of plain `Statement` strings. This is a crucial security practice to prevent **SQL Injection** attacks, as `PreparedStatement` automatically escapes dangerous user input.
- **Swing UI Framework**: We use core components like `JPanel`, `JFrame`, `JButton`, and `JTable`. We utilize layout managers (like `BorderLayout` and `GridLayout`) to make the app scale and resize gracefully depending on the screen dimensions.
- **Concurrency (`SwingWorker`)**: Java UI runs on the EDT (Event Dispatch Thread). If we do a long network request (like downloading food images from an API) on the EDT, the entire app freezes. `SwingWorker` fixes this by moving the download to a background thread and then passing the finished image back to the UI thread safely.
- **Object-Oriented Programming (OOP)**: The project heavily uses Encapsulation (using `private` fields in Models with public Getters/Setters) and modular class design.

## 5. Summary

To summarize the flow in one sentence:
_"This is an MVC-based Java Swing application utilizing a structured MySQL backend, where UI interaction dynamically triggers JDBC Controller transactions to manage restaurant seating, process orders securely, calculate synchronized tax/discount billing, and aggregate administrative revenue reports."_
