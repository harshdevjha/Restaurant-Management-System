package com.restaurant.view;

import com.restaurant.controller.AuthController;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardFrame – the main application window with a sidebar and CardLayout
 * content area.
 * The sidebar navigation is role-sensitive: Admin sees all panels, Staff sees
 * Tables/Orders.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Panel names used with CardLayout
    public static final String PANEL_TABLES = "TABLES";
    public static final String PANEL_ADMIN = "ADMIN";
    public static final String PANEL_STAFF = "STAFF";
    public static final String PANEL_REPORTS = "REPORTS";
    public static final String PANEL_BILLS = "BILLS";

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("Restaurant Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.CONTENT_BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Top bar ──────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setPreferredSize(new Dimension(0, 54));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel logo = UITheme.createLabel("🍽  Restaurant Management System",
                UITheme.FONT_HEADING, Color.WHITE);
        JLabel user = UITheme.createLabel("Logged in: " + currentUser.getFullName()
                + "  |  " + currentUser.getRole(),
                UITheme.FONT_SMALL, new Color(0xFFCCBB));
        JButton logout = UITheme.createButton("Logout", UITheme.ACCENT, Color.WHITE);
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AuthController.logout();
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
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
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));

        addNavButton(sidebar, "🪑  Tables & Orders", PANEL_TABLES, true);

        if (currentUser.isAdmin()) {
            addNavButton(sidebar, "🍕  Menu Management", PANEL_ADMIN, false);
            addNavButton(sidebar, "👥  Staff Accounts", PANEL_STAFF, false);
            addNavButton(sidebar, "🧾  Bill History", PANEL_BILLS, false);
            addNavButton(sidebar, "📊  Sales Reports", PANEL_REPORTS, false);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String label, String panelName, boolean active) {
        JButton btn = UITheme.createSidebarButton(label);
        if (active)
            btn.setBackground(UITheme.SIDEBAR_HOVER);
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, panelName);
            // refresh table layout on switch
            if (PANEL_TABLES.equals(panelName)) {
                Component comp = getCurrentCard();
                if (comp instanceof TableLayoutPanel)
                    ((TableLayoutPanel) comp).refresh();
            }
        });
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private Component getCurrentCard() {
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible())
                return c;
        }
        return null;
    }

    // ── Content panels ───────────────────────────────────────────
    private JPanel buildContent() {
        contentPanel.setBackground(UITheme.CONTENT_BG);

        contentPanel.add(new TableLayoutPanel(this, currentUser), PANEL_TABLES);

        if (currentUser.isAdmin()) {
            contentPanel.add(new AdminMenuPanel(), PANEL_ADMIN);
            contentPanel.add(new StaffPanel(), PANEL_STAFF);
            contentPanel.add(new BillHistoryPanel(), PANEL_BILLS);
            contentPanel.add(new ReportsPanel(), PANEL_REPORTS);
        }

        cardLayout.show(contentPanel, PANEL_TABLES);
        return contentPanel;
    }
}
