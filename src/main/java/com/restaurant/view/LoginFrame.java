package com.restaurant.view;

import com.restaurant.controller.AuthController;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame – Resolves user authentication; acts as the absolute application entry point.
 * Constructs a visually centered login card featuring restaurant gradients and branding.
 * Intercepts inputs to validate credentials via the AuthController.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    /** Constructor preparing the unauthenticated viewport constraints */
    public LoginFrame() {
        setTitle("Restaurant Management System – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null); // Center application on monitor
        setResizable(false);
        initUI();
    }

    /** Programmatic layout instantiation configuring background, fonts, spacing, and buttons */
    private void initUI() {
        // ── Background panel ────────────────────────────────────
        // Custom background overriding paintComponent to inject a full bleed CSS-style gradient
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Radial/Linear gradient blending UITheme parameters organically
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK,
                        getWidth(), getHeight(), UITheme.ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        setContentPane(bg);

        // ── Login card ──────────────────────────────────────────
        // The modal-like box holding inputs, overlaid structurally upon the background gradient
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw soft drop shadow underneath card
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(4, 6, getWidth() - 4, getHeight() - 4, 20, 20);
                // Draw primary solid white foreground panel
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        // Enforce strict vertical stacking rules for textfields
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 400));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        // Logo / title headers utilizing common UITheme styles
        JLabel icon = UITheme.createLabel("🍽", new Font("SansSerif", Font.PLAIN, 48), UITheme.PRIMARY);
        JLabel title = UITheme.createLabel("Restaurant Manager", UITheme.FONT_TITLE, UITheme.PRIMARY);
        JLabel sub = UITheme.createLabel("Sign in to continue", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
        
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields inputs
        JLabel userLbl = UITheme.createLabel("Username", UITheme.FONT_BTN, UITheme.TEXT_DARK);
        usernameField = UITheme.createTextField(20);

        JLabel passLbl = UITheme.createLabel("Password", UITheme.FONT_BTN, UITheme.TEXT_DARK);
        passwordField = UITheme.createPasswordField(20);

        // Login button mapped via UITheme factory
        JButton loginBtn = UITheme.createButton("Login", UITheme.PRIMARY, Color.WHITE);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(280, 40));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Status label for reflecting credential rejection visually without popups
        statusLabel = UITheme.createLabel("", UITheme.FONT_SMALL, UITheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble card sequence vertically appending spacers manually
        card.add(icon);
        card.add(Box.createVerticalStrut(4));
        card.add(title);
        card.add(Box.createVerticalStrut(2));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);

        bg.add(card);

        // ── Event handlers ──────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin()); // Allows user to smash 'Enter' key resolving Auth
        usernameField.addActionListener(e -> passwordField.requestFocus()); // 'Enter' shifts cursor context
    }

    /**
     * Executes the verification process querying the underlying controller.
     * Transitions window control flow directly to the Dashboard screen upon success.
     */
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Basic front-end client-side presence validation
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        // Delegate backend credential matching against the SQL instance
        User user = AuthController.login(username, password);
        
        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText(""); // Scrub password input visually 
        } else {
            statusLabel.setText("");
            dispose(); // Cleanly destroy the authentication framing
            
            // Queue main execution loop swapping view to standard dashboard layout
            SwingUtilities.invokeLater(() -> new DashboardFrame(user).setVisible(true));
        }
    }
}
