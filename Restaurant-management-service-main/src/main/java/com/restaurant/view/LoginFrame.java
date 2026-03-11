package com.restaurant.view;

import com.restaurant.controller.AuthController;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame – the application entry screen.
 * Displays a centred login card with restaurant branding.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;

    public LoginFrame() {
        setTitle("Restaurant Management System – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        // ── Background panel ────────────────────────────────────
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK,
                                              getWidth(), getHeight(), UITheme.ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        setContentPane(bg);

        // ── Login card ──────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(4, 6, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 400));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        // Logo / title
        JLabel icon  = UITheme.createLabel("🍽", new Font("SansSerif", Font.PLAIN, 48), UITheme.PRIMARY);
        JLabel title = UITheme.createLabel("Restaurant Manager", UITheme.FONT_TITLE, UITheme.PRIMARY);
        JLabel sub   = UITheme.createLabel("Sign in to continue", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields
        JLabel userLbl = UITheme.createLabel("Username", UITheme.FONT_BTN, UITheme.TEXT_DARK);
        usernameField  = UITheme.createTextField(20);

        JLabel passLbl = UITheme.createLabel("Password", UITheme.FONT_BTN, UITheme.TEXT_DARK);
        passwordField  = UITheme.createPasswordField(20);

        // Login button
        JButton loginBtn = UITheme.createButton("Login", UITheme.PRIMARY, Color.WHITE);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(280, 40));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Status label
        statusLabel = UITheme.createLabel("", UITheme.FONT_SMALL, UITheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble card
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
        passwordField.addActionListener(e -> doLogin());    // Enter key
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        User user = AuthController.login(username, password);
        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
        } else {
            statusLabel.setText("");
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame(user).setVisible(true));
        }
    }
}
