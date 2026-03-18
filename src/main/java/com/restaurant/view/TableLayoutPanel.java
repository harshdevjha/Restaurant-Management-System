package com.restaurant.view;

import com.restaurant.controller.OrderController;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TableLayoutPanel – Dynamic grid interface rendering the restaurant's physical floor layout.
 * Visual cues dictate table availability (Green/FREE vs Red/OCCUPIED).
 * Clicking a FREE table provisions a new order session.
 * Clicking an OCCUPIED table re-opens the active transactional OrderPanel.
 */
public class TableLayoutPanel extends JPanel {

    private final DashboardFrame dashboard;
    private final User currentUser;
    private final JPanel grid;

    public TableLayoutPanel(DashboardFrame dashboard, User user) {
        this.dashboard = dashboard;
        this.currentUser = user;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Header & Controls ──────────────────────────────────────────
        JLabel heading = UITheme.createLabel("🪑  Table Layout", UITheme.FONT_TITLE, UITheme.PRIMARY);
        JButton refresh = UITheme.createButton("↻  Refresh", UITheme.ACCENT, Color.WHITE);
        refresh.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ── Legend ─────────────────────────────────────────────────────
        // Provides visual context decoding the color scheme
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legend.setOpaque(false);
        legend.add(makeChip("● FREE", UITheme.SUCCESS));
        legend.add(makeChip("● OCCUPIED", UITheme.DANGER));
        add(legend, BorderLayout.SOUTH);

        // ── Interactive Table Grid ─────────────────────────────────────
        // Auto-wrapping GridLayout scaling logically as the window resizes
        grid = new JPanel(new GridLayout(0, 5, 14, 14));
        grid.setOpaque(false);
        
        // Wrap grid in a scroll pane guarding against layout overflow on smaller monitors
        JScrollPane scroll = new JScrollPane(grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // Bootstrap initial data load
        refresh();
    }

    /** 
     * Erases the current volatile UI state, queries the database for synchronous 
     * table statuses, and redraws the physical cards.
     */
    public void refresh() {
        grid.removeAll();
        List<RestaurantTable> tables = OrderController.getAllTables();
        for (RestaurantTable t : tables) {
            grid.add(buildTableCard(t));
        }
        grid.revalidate(); // Instructs Swing layout manager to recalculate bounds
        grid.repaint();    // Schedules a visual flush to the screen
    }

    /**
     * Factory constructing an interactive visual card representing a single restaurant table.
     * Incorporates nested layouts, dynamically colored borders, and hover-state listeners.
     */
    private JPanel buildTableCard(RestaurantTable t) {
        boolean free = t.isFree();
        
        // Dynamically assign thematic palette based exclusively on functional availability
        Color cardColor = free ? new Color(0xE8F5E9) : new Color(0xFFEBEE);
        Color borderColor = free ? UITheme.SUCCESS : UITheme.DANGER;
        Color textColor = free ? UITheme.SUCCESS : UITheme.DANGER;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Base background fill
                g2.setColor(cardColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Surrounding status border stroke
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(140, 170));
        card.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Display textual properties
        JLabel tableNum = UITheme.createLabel("Table " + t.getNumber(),
                UITheme.FONT_HEADING, UITheme.TEXT_DARK);
        JLabel cap = UITheme.createLabel("Seats: " + t.getCapacity(),
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        JLabel status = UITheme.createLabel(t.getStatus().name(), UITheme.FONT_BTN, textColor);
        
        tableNum.setAlignmentX(Component.CENTER_ALIGNMENT);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Emergency manual override toggle for edge cases (e.g. reserving a table remotely)
        JButton toggleBtn = UITheme.createButton(
                free ? "Mark Occupied" : "Mark Free",
                free ? UITheme.DANGER : UITheme.SUCCESS,
                Color.WHITE);
        toggleBtn.setFont(new Font("SansSerif", Font.PLAIN, 10)); // small text
        toggleBtn.setPreferredSize(new Dimension(110, 24));
        toggleBtn.setMaximumSize(new Dimension(110, 24));
        toggleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggleBtn.addActionListener(e -> {
            try {
                // Flip boolean status and cast back to ENUM
                OrderController.updateTableStatus(t.getId(),
                        free ? RestaurantTable.Status.OCCUPIED : RestaurantTable.Status.FREE);
                refresh(); // Synchronize UI with modified backend state
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(TableLayoutPanel.this, "Failed to update status.");
            }
        });

        // Pack elements vertically leveraging invisible struts/glue for proportional spacing
        card.add(Box.createVerticalGlue());
        card.add(tableNum);
        card.add(Box.createVerticalStrut(4));
        card.add(cap);
        card.add(Box.createVerticalStrut(8));
        card.add(status);
        card.add(Box.createVerticalStrut(8));
        card.add(toggleBtn);
        card.add(Box.createVerticalGlue());

        // ── Card Interaction Listeners ─────────────────────────────
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Ignore clicks originating intrinsically from the override toggle button
                if (e.getSource() != toggleBtn && !SwingUtilities.isDescendingFrom(e.getComponent(), toggleBtn)) {
                    openOrder(t);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Simulate a visual "bump" by temporarily altering inset borders on hover
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(13, 9, 13, 9),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Restore standard insets when cursor leaves boundary
                card.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
            }
        });

        return card;
    }

    /**
     * Mounts and displays a modal JDialog intercepting the application flow to handle 
     * specific transactional ordering logic tied to this exact table instance.
     */
    private void openOrder(RestaurantTable table) {
        OrderPanel orderPanel = new OrderPanel(table, currentUser, this);
        JDialog dialog = new JDialog(dashboard, "Order — Table " + table.getNumber(), true);
        dialog.setSize(980, 620);
        dialog.setLocationRelativeTo(dashboard);
        dialog.add(orderPanel);
        
        // Critical teardown hook ensuring the Grid layout acknowledges any billing status changes
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refresh(); // refresh table status after bill generation or order clearance
            }
        });
        dialog.setVisible(true); // Halts parent async execution until Dialog explicitly disposed
    }

    /** Convenience aesthetic generator */
    private JLabel makeChip(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(color);
        return lbl;
    }
}
