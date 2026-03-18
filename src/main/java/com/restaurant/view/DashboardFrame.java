package com.restaurant.view;

import com.restaurant.controller.AuthController;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardFrame – Single-Page Application (SPA) style overarching Swing frame.
 * Orchestrates rendering using a CardLayout to actively swap out interior screens
 * while maintaining a persistent static TopBar and Sidebar mapping interaction structure.
 * Sidebar visibility scales dynamically scaling accessible modules against the actor's system Role.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    // Centralized manager enabling instantaneous swapping of deeply grouped components
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Static registry mapping explicitly named panel references used by CardLayout switching mechanism
    public static final String PANEL_TABLES = "TABLES";
    public static final String PANEL_ADMIN = "ADMIN";
    public static final String PANEL_STAFF = "STAFF";
    public static final String PANEL_REPORTS = "REPORTS";
    public static final String PANEL_BILLS = "BILLS";

    /** Master instantiation linking the session-authorized actor User */
    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("Restaurant Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600)); // Enforce scaling bounds to avoid UI clipping
        setLocationRelativeTo(null);
        initUI();
    }

    /** Primary layout construction assembling the Top/Left/Center application scaffold */
    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.CONTENT_BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Top bar ──────────────────────────────────────────────────
    /** Generates the persistent utility banner featuring session identity constraints */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setPreferredSize(new Dimension(0, 54));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel logo = UITheme.createLabel("🍽  Restaurant Management System",
                UITheme.FONT_HEADING, Color.WHITE);
                
        // Display credentials assuring operators they are logged in appropriately
        JLabel user = UITheme.createLabel("Logged in: " + currentUser.getFullName()
                + "  |  " + currentUser.getRole(),
                UITheme.FONT_SMALL, new Color(0xFFCCBB));
                
        // Clear session trace globally when destroying UI parameters
        JButton logout = UITheme.createButton("Logout", UITheme.ACCENT, Color.WHITE);
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AuthController.logout();
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Return back to portal lock step
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(logout);

        bar.add(logo, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ──────────────────────────────────────────────────
    /** 
     * Constructs the vertical menu column. Applies Role-Based Access Control filtering. 
     * STAFF accounts exclusively see Table operations, omitting Admin panels entirely.
     */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); // Vertical scaling 
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));

        // Available universally
        addNavButton(sidebar, "🪑  Tables & Orders", PANEL_TABLES, true);

        // Strict role validation checking rendering backend systems
        if (currentUser.isAdmin()) {
            addNavButton(sidebar, "🍕  Menu Management", PANEL_ADMIN, false);
            addNavButton(sidebar, "👥  Staff Accounts", PANEL_STAFF, false);
            addNavButton(sidebar, "🧾  Bill History", PANEL_BILLS, false);
            addNavButton(sidebar, "📊  Sales Reports", PANEL_REPORTS, false);
        }

        // Fills residual vertical space packing nav tightly upwards 
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /** Utility binder routing a sidebar button click seamlessly toward a distinct panel map id */
    private void addNavButton(JPanel sidebar, String label, String panelName, boolean active) {
        JButton btn = UITheme.createSidebarButton(label);
        if (active)
            btn.setBackground(UITheme.SIDEBAR_HOVER); // Initial boot-up marker
            
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, panelName);
            // Re-fetch synchronous table states automatically resolving out-of-sync visual anomalies
            if (PANEL_TABLES.equals(panelName)) {
                Component comp = getCurrentCard();
                if (comp instanceof TableLayoutPanel)
                    ((TableLayoutPanel) comp).refresh(); // Custom UI redraw abstraction
            }
        });
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    /** Interrogates the active CardLayout components to isolate the single actively displayed canvas */
    private Component getCurrentCard() {
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible())
                return c;
        }
        return null;
    }

    // ── Content panels ───────────────────────────────────────────
    /**
     * Bootstraps raw references into the CardLayout namespace mappings. 
     * Honors access restrictions precisely identical to the Sidebar binding sequence.
     */
    private JPanel buildContent() {
        contentPanel.setBackground(UITheme.CONTENT_BG);

        // Core transactional view universally appended
        contentPanel.add(new TableLayoutPanel(this, currentUser), PANEL_TABLES);

        // Admin-strict hierarchy appended solely when privilege checks pass
        if (currentUser.isAdmin()) {
            contentPanel.add(new AdminMenuPanel(), PANEL_ADMIN);
            contentPanel.add(new StaffPanel(), PANEL_STAFF);
            contentPanel.add(new BillHistoryPanel(), PANEL_BILLS);
            contentPanel.add(new ReportsPanel(), PANEL_REPORTS);
        }

        // Force Table layout to automatically render natively upon dashboard spawn
        cardLayout.show(contentPanel, PANEL_TABLES);
        return contentPanel;
    }
}
