package com.restaurant;

import com.restaurant.db.DBConnection;
import com.restaurant.view.LoginFrame;
import com.restaurant.view.UITheme;

import javax.swing.*;

/**
 * App – Central application entry point orchestrating structural initialization.
 * 
 * Responsibilities include:
 * 1. Establishing Native/Cross-Platform Window aesthetics (Nimbus Look & Feel).
 * 2. Hydrating UI Defaults enforcing global application thematic consistency.
 * 3. Validating external persistent dependencies (MySQL Database) prior to execution.
 * 4. Launching the primary graphical authentication interface securely.
 * 5. Binding graceful environment shutdown handlers releasing system resources.
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

        // 1. Set Nimbus look-and-feel explicitly for a modern standardized JVM appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Silently fallback to native system L&F preventing cosmetic degradation from crashing app
            System.err.println("Nimbus L&F not available, using default system UI.");
        }

        // 2. Map and inject custom thematic color/font templates into absolute Swing global scope
        UITheme.applyDefaults();

        // 3. Eagerly probe DB connectivity proactively failing fast if external conditions aren't met
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            // Unrecoverable state: Output verbose HTML-formatted troubleshooting guide mapping to prerequisites
            JOptionPane.showMessageDialog(null,
                    "<html><b>Cannot connect to MySQL database.</b><br><br>"
                            + "Please ensure:<br>"
                            + "• MySQL is running natively on localhost:3306<br>"
                            + "• Database 'restaurant_db' schema exists (run sql/schema.sql)<br>"
                            + "• Administrative Credentials in DBConnection.java match precisely<br><br>"
                            + "<i>Error Details: " + e.getMessage() + "</i></html>",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            // Terminate native OS process abruptly returning exit-code 1 mitigating headless zombies
            System.exit(1);
        }

        // 4. Delegate UI generation execution exclusively to Java's Event Dispatch Thread 
        // avoiding concurrent race condition mutation of graphical components
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true); // Expose initial locked boundary requesting authentication
        });

        // 5. Register native OS interceptor capturing CTRL+C or graceful termination signals
        // Executes bounded lambda immediately severing persistent network pools
        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeConnection));
    }
}
