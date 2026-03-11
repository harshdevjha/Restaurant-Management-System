# Restaurant Management System

A comprehensive Java-based desktop application for managing restaurant operations including billing, orders, inventory, and staff management.

## Features

- **User Authentication**: Secure login system with role-based access control
- **Order Management**: Create and track customer orders with real-time updates
- **Bill Generation**: Automated bill creation and history tracking
- **Menu Management**: Easy-to-manage restaurant menu with categories and items
- **Table Management**: Track table occupancy and reservations
- **Admin Dashboard**: Comprehensive dashboard for administrators with reports and analytics
- **Staff Management**: Manage staff and their roles
- **Reports & Analytics**: Generate sales reports and business insights

## Project Structure

```
src/main/java/
├── com/restaurant/
│   ├── App.java                          # Main application entry point
│   ├── controller/                       # Business logic controllers
│   │   ├── AdminController.java
│   │   ├── AuthController.java
│   │   ├── BillController.java
│   │   ├── MenuController.java
│   │   ├── OrderController.java
│   │   └── ReportController.java
│   ├── db/                               # Database utilities
│   │   └── DBConnection.java
│   ├── model/                            # Data models
│   │   ├── Bill.java
│   │   ├── Category.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── RestaurantTable.java
│   │   └── User.java
│   └── view/                             # UI components
│       ├── AdminMenuPanel.java
│       ├── BillDialog.java
│       ├── BillHistoryPanel.java
│       ├── DashboardFrame.java
│       ├── LoginFrame.java
│       ├── OrderPanel.java
│       ├── ReportsPanel.java
│       ├── StaffPanel.java
│       ├── TableLayoutPanel.java
│       └── UITheme.java
└── TestBill.java                         # Test utilities

sql/
└── schema.sql                             # Database schema
```

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- Database: MySQL/MariaDB
- Operating System: Windows, macOS, or Linux

## Setup Instructions

### 1. Database Setup

Import the database schema:
```bash
mysql -u <username> -p <database_name> < sql/schema.sql
```

### 2. Configuration

Update database connection details in `com/restaurant/db/DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### 3. Build & Run

Compile the project:
```bash
javac -d bin -sourcepath src/main/java src/main/java/com/restaurant/App.java
```

Run the application:
```bash
java -cp bin:lib/* com.restaurant.App
```

## Technologies Used

- **Language**: Java
- **Database**: MySQL
- **GUI Framework**: Java Swing
- **Architecture**: MVC (Model-View-Controller)

## Version

Current Version: 1.0.0

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Contact

For questions or support, please reach out to the project maintainer.

---

**Note**: This is a desktop application and requires proper database configuration before running.
