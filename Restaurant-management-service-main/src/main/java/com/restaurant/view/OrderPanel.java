package com.restaurant.view;

import com.restaurant.controller.BillController;
import com.restaurant.controller.MenuController;
import com.restaurant.controller.OrderController;
import com.restaurant.model.Bill;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * OrderPanel – split screen:
 * LEFT — category tabs + scrollable menu items with "Add" buttons
 * RIGHT — cart (order items) with qty controls + "Generate Bill" button
 */
public class OrderPanel extends JPanel {

    private final RestaurantTable table;
    private final User currentUser;
    private final TableLayoutPanel parent;

    private Order currentOrder;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTable cartTable;

    public OrderPanel(RestaurantTable table, User user, TableLayoutPanel parent) {
        this.table = table;
        this.currentUser = user;
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(UITheme.CONTENT_BG);

        // Load or create order
        currentOrder = OrderController.getActiveOrderForTable(table.getId());
        if (currentOrder == null) {
            int orderId = OrderController.createOrder(table.getId(), user.getId());
            currentOrder = OrderController.getActiveOrderForTable(table.getId());
            if (currentOrder == null) {
                // fallback - build a transient order shell
                currentOrder = new Order();
                currentOrder.setId(orderId);
                currentOrder.setTableId(table.getId());
                currentOrder.setTableNumber(table.getNumber());
                currentOrder.setUserId(user.getId());
                currentOrder.setStatus(Order.Status.PENDING);
                currentOrder.setItems(new java.util.ArrayList<>());
            }
        }

        initUI();
        refreshCart();
    }

    private void initUI() {
        // ── Header ──────────────────────────────────────────────
        JLabel heading = UITheme.createLabel(
                "Order for Table " + table.getNumber() + "  (Seats: " + table.getCapacity() + ")",
                UITheme.FONT_HEADING, UITheme.PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        heading.setForeground(Color.WHITE);
        header.add(heading, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Split pane ───────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMenuPanel(), buildCartPanel());
        split.setDividerLocation(560);
        split.setDividerSize(4);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ── Left: Menu panel ──────────────────────────────────────────
    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 8));

        JLabel lbl = UITheme.createLabel("Menu", UITheme.FONT_HEADING, UITheme.TEXT_DARK);
        panel.add(lbl, BorderLayout.NORTH);

        List<Category> categories = MenuController.getAllCategories();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BTN);
        tabs.setBackground(UITheme.CONTENT_BG);
        tabs.setForeground(UITheme.PRIMARY);

        for (Category cat : categories) {
            tabs.addTab(cat.getName(), buildCategoryTab(cat));
        }
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCategoryTab(Category cat) {
        JPanel itemGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        itemGrid.setBackground(UITheme.CONTENT_BG);
        itemGrid.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        List<MenuItem> items = MenuController.getItemsByCategory(cat.getId());
        for (MenuItem item : items) {
            itemGrid.add(buildMenuItemCard(item));
        }

        JScrollPane scroll = new JScrollPane(itemGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        return scroll;
    }

    private JPanel buildMenuItemCard(MenuItem item) {
        JPanel card = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0xEEDDCC));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel name = UITheme.createLabel(item.getName(), UITheme.FONT_BTN, UITheme.TEXT_DARK);
        JLabel price = UITheme.createLabel("₹ " + String.format("%.2f", item.getPrice()),
                UITheme.FONT_BODY, UITheme.ACCENT);
        JLabel desc = UITheme.createLabel(
                "<html><small>" + (item.getDescription() != null ? item.getDescription() : "") + "</small></html>",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED);

        JButton addBtn = UITheme.createButton("+ Add", UITheme.PRIMARY, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(70, 28));
        addBtn.addActionListener(e -> addToCart(item));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(name, BorderLayout.WEST);
        top.add(price, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(addBtn, BorderLayout.EAST);

        return card;
    }

    // ── Right: Cart panel ─────────────────────────────────────────
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));

        JLabel lbl = UITheme.createLabel("Cart", UITheme.FONT_HEADING, UITheme.TEXT_DARK);

        // Cart table
        cartModel = new DefaultTableModel(new String[] { "Item", "Qty", "Price", "Subtotal" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDC8B8)));

        // Footer: total + controls
        totalLabel = UITheme.createLabel("Total: ₹ 0.00", UITheme.FONT_HEADING, UITheme.PRIMARY);

        JButton removeBtn = UITheme.createButton("Remove Selected", UITheme.DANGER, Color.WHITE);
        removeBtn.addActionListener(e -> removeSelectedItem());

        JButton billBtn = UITheme.createButton("Generate Bill 🧾", UITheme.SUCCESS, Color.WHITE);
        billBtn.addActionListener(e -> generateBill());

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.add(totalLabel, BorderLayout.NORTH);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(removeBtn);
        btnRow.add(billBtn);
        footer.add(btnRow, BorderLayout.SOUTH);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // ── Actions ──────────────────────────────────────────────────

    private void addToCart(MenuItem item) {
        OrderController.addItemToOrder(currentOrder.getId(), item.getId(),
                item.getName(), item.getPrice(), 1);
        refreshCart();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }
        List<OrderItem> items = OrderController.getOrderItems(currentOrder.getId());
        if (row < items.size()) {
            OrderController.removeOrderItem(items.get(row).getId());
            refreshCart();
        }
    }

    private void generateBill() {
        List<OrderItem> items = OrderController.getOrderItems(currentOrder.getId());
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items before generating a bill.");
            return;
        }

        // Prompt for discount
        String discStr = JOptionPane.showInputDialog(this,
                "Enter Discount % (0 for none):", "Discount", JOptionPane.QUESTION_MESSAGE);
        if (discStr == null)
            return;
        double discPct = 0;
        try {
            discPct = Double.parseDouble(discStr);
        } catch (NumberFormatException ignored) {
        }
        discPct = Math.max(0, Math.min(100, discPct));

        double taxPct = 5.0; // 5% GST
        Bill bill = BillController.generateBill(currentOrder.getId(), table.getId(), discPct, taxPct);

        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Error generating bill. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the receipt dialog parented to the main dashboard (not the order panel which we are about to destroy)
        Frame mainFrame = (Frame) SwingUtilities.getWindowAncestor(parent);
        BillDialog dialog = new BillDialog(mainFrame, bill, items, table.getNumber());
        
        // Close order panel window, refresh table layout
        SwingUtilities.getWindowAncestor(this).dispose();
        parent.refresh();
        
        // Show receipt dialog
        dialog.setVisible(true);
    }

    /** Reloads cart table from DB and updates total label. */
    private void refreshCart() {
        cartModel.setRowCount(0);
        List<OrderItem> items = OrderController.getOrderItems(currentOrder.getId());
        double total = 0;
        for (OrderItem oi : items) {
            cartModel.addRow(new Object[] {
                    oi.getMenuItemName(),
                    oi.getQuantity(),
                    String.format("₹ %.2f", oi.getUnitPrice()),
                    String.format("₹ %.2f", oi.getSubtotal())
            });
            total += oi.getSubtotal();
        }
        totalLabel.setText(String.format("Total: ₹ %.2f", total));
    }
}
