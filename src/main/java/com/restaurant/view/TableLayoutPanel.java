package com.restaurant.view;

import com.restaurant.controller.OrderController;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TableLayoutPanel – displays all restaurant tables in a grid.
 * Green card = FREE, Red card = OCCUPIED.
 * Clicking an OCCUPIED table re-opens its active order.
 * Clicking a FREE table creates a new order.
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

        // Header
        JLabel heading = UITheme.createLabel("🪑  Table Layout", UITheme.FONT_TITLE, UITheme.PRIMARY);
        JButton refresh = UITheme.createButton("↻  Refresh", UITheme.ACCENT, Color.WHITE);
        refresh.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legend.setOpaque(false);
        legend.add(makeChip("● FREE", UITheme.SUCCESS));
        legend.add(makeChip("● OCCUPIED", UITheme.DANGER));
        add(legend, BorderLayout.SOUTH);

        // Table grid
        grid = new JPanel(new GridLayout(0, 5, 14, 14));
        grid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    /** Reloads table statuses from DB and repaints cards. */
    public void refresh() {
        grid.removeAll();
        List<RestaurantTable> tables = OrderController.getAllTables();
        for (RestaurantTable t : tables) {
            grid.add(buildTableCard(t));
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel buildTableCard(RestaurantTable t) {
        boolean free = t.isFree();
        Color cardColor = free ? new Color(0xE8F5E9) : new Color(0xFFEBEE);
        Color borderColor = free ? UITheme.SUCCESS : UITheme.DANGER;
        Color textColor = free ? UITheme.SUCCESS : UITheme.DANGER;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
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

        JLabel tableNum = UITheme.createLabel("Table " + t.getNumber(),
                UITheme.FONT_HEADING, UITheme.TEXT_DARK);
        JLabel cap = UITheme.createLabel("Seats: " + t.getCapacity(),
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        JLabel status = UITheme.createLabel(t.getStatus().name(), UITheme.FONT_BTN, textColor);
        tableNum.setAlignmentX(Component.CENTER_ALIGNMENT);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

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
                OrderController.updateTableStatus(t.getId(),
                        free ? RestaurantTable.Status.OCCUPIED : RestaurantTable.Status.FREE);
                refresh();
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(TableLayoutPanel.this, "Failed to update status.");
            }
        });

        card.add(Box.createVerticalGlue());
        card.add(tableNum);
        card.add(Box.createVerticalStrut(4));
        card.add(cap);
        card.add(Box.createVerticalStrut(8));
        card.add(status);
        card.add(Box.createVerticalStrut(8));
        card.add(toggleBtn);
        card.add(Box.createVerticalGlue());

        // Click → open order panel
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // If they clicked the toggle button, it handles its own action. Let's not
                // double-trigger openOrder.
                if (e.getSource() != toggleBtn && !SwingUtilities.isDescendingFrom(e.getComponent(), toggleBtn)) {
                    openOrder(t);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(13, 9, 13, 9),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
            }
        });

        return card;
    }

    private void openOrder(RestaurantTable table) {
        OrderPanel orderPanel = new OrderPanel(table, currentUser, this);
        JDialog dialog = new JDialog(dashboard, "Order — Table " + table.getNumber(), true);
        dialog.setSize(980, 620);
        dialog.setLocationRelativeTo(dashboard);
        dialog.add(orderPanel);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refresh(); // refresh table status after bill generation or order clearance
            }
        });
        dialog.setVisible(true);
    }

    private JLabel makeChip(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(color);
        return lbl;
    }
}
