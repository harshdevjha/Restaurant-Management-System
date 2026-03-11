package com.restaurant;

import com.restaurant.db.DBConnection;
import com.restaurant.view.LoginFrame;
import com.restaurant.view.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * App – application entry point.
 *
 * Compile:
 * javac -cp "lib/*" -d out $(find src -name "*.java")
 *
 * Run:
 * java -cp "out:lib/*" com.restaurant.App
 * (Windows: use semicolons instead of colons "out;lib/*")
 */
public class App {

    public static void main(String[] args) {

        // Set Nimbus look-and-feel for a modern default appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Falls back to system L&F — not critical
            System.err.println("Nimbus L&F not available, using default.");
        }

        // Apply custom theme defaults
        UITheme.applyDefaults();

        // Verify DB connectivity before showing the UI
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Cannot connect to MySQL database.</b><br><br>"
                            + "Please ensure:<br>"
                            + "• MySQL is running on localhost:3306<br>"
                            + "• Database 'restaurant_db' exists (run sql/schema.sql)<br>"
                            + "• Credentials in DBConnection.java are correct<br><br>"
                            + "<i>Error: " + e.getMessage() + "</i></html>",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });

        // Close DB connection on exit
        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeConnection));
    }
}
