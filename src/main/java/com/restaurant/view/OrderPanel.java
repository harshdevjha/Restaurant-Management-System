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
 * OrderPanel – Comprehensive transactional viewport facilitating the ordering process.
 * Constructed via an adjustable JSplitPane:
 * LEFT PANE: Horizontally tabbed menu categories featuring generated item cards.
 * RIGHT PANE: Real-time 'Cart' reflection containing queued line items, prices, and checkout hooks.
 */
public class OrderPanel extends JPanel {

    private final RestaurantTable table;
    private final TableLayoutPanel parent;

    private Order currentOrder;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTable cartTable;

    /** 
     * Constructor anchoring the panel to a physical restaurant table constraint.
     */
    public OrderPanel(RestaurantTable table, User user, TableLayoutPanel parent) {
        this.table = table;
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(UITheme.CONTENT_BG);

        // ── Order Synchronization ──────────────────────────────
        // Interrogate database assessing if this table inherently possesses an active session
        currentOrder = OrderController.getActiveOrderForTable(table.getId());
        if (currentOrder == null) {
            // Provision brand new physical grouping inside 'orders' DB
            int orderId = OrderController.createOrder(table.getId(), user.getId());
            currentOrder = OrderController.getActiveOrderForTable(table.getId());
            if (currentOrder == null) {
                // Failsafe fallback - build a transient order shell incase DB fetch anomalously fails
                currentOrder = new Order();
                currentOrder.setId(orderId);
                currentOrder.setTableId(table.getId());
                currentOrder.setTableNumber(table.getNumber());
                currentOrder.setUserId(user.getId());
                currentOrder.setStatus(Order.Status.PENDING);
                currentOrder.setItems(new java.util.ArrayList<>());
            }
        }

        // Ensure table status is instantly marked Occupied visually for the user and in the DB
        try {
            OrderController.updateTableStatus(table.getId(), RestaurantTable.Status.OCCUPIED);
        } catch (Exception e) {
            e.printStackTrace(); // Swallow exceptions securely in UI scope
        }
        parent.refresh(); // Tell parent TableGrid to paint red

        initUI();
        refreshCart();
    }

    /** Structurally maps and aligns interior panes. */
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
        // Horizontal orientation allocating larger bias natively toward the menu selection portion
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMenuPanel(), buildCartPanel());
        split.setDividerLocation(560);
        split.setDividerSize(4);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ── Left: Menu panel ──────────────────────────────────────────
    /** Constructs the tabular taxonomy containing distinct menu categories. */
    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 8));

        JLabel lbl = UITheme.createLabel("Menu", UITheme.FONT_HEADING, UITheme.TEXT_DARK);
        panel.add(lbl, BorderLayout.NORTH);

        // Inject dynamic Category classification directly from schema
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

    /** Embeds category-specific items inside a vertical scrolling pane layout. */
    private JScrollPane buildCategoryTab(Category cat) {
        JPanel itemGrid = new JPanel(new GridLayout(0, 1, 10, 10)); // Single wide column
        itemGrid.setBackground(UITheme.CONTENT_BG);
        itemGrid.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        List<MenuItem> items = MenuController.getItemsByCategory(cat.getId());
        for (MenuItem item : items) {
            itemGrid.add(buildMenuItemCard(item, cat.getName()));
        }

        JScrollPane scroll = new JScrollPane(itemGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        return scroll;
    }

    /** 
     * Assembles a modular, richly-styled interactive card dictating a single purchasable Item. 
     * Automatically attempts to resolve network images simulating real-world aesthetic.
     */
    private JPanel buildMenuItemCard(MenuItem item, String categoryName) {
        JPanel card = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0xEEDDCC));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel name = UITheme.createLabel("<html>" + item.getName() + "</html>", UITheme.FONT_BTN, UITheme.TEXT_DARK);
        JLabel price = UITheme.createLabel("₹ " + String.format("%.2f", item.getPrice()),
                UITheme.FONT_BODY, UITheme.ACCENT);
                
        // Utilize HTML parsing natively inside JLabel to enforce multi-line wrapping constraints
        JLabel desc = UITheme.createLabel(
                "<html><small>" + (item.getDescription() != null ? item.getDescription() : "") + "</small></html>",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED);

        // Transaction hook registering selections
        JButton addBtn = UITheme.createButton("+ Add", UITheme.PRIMARY, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(70, 28));
        addBtn.addActionListener(e -> addToCart(item));

        // ── Image Placeholder Rendering ───────────────────────
        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 80, 80));
                if (getIcon() != null) { // Render network texture if available
                    getIcon().paintIcon(this, g2, 0, 0);
                } else { // Fallback paints solid circle
                    g2.setColor(getBackground());
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    super.paintComponent(g2);
                }
                g2.dispose();
            }
        };
        
        // Circular profile constraint mapping
        imageLabel.setOpaque(false);
        imageLabel.setBackground(new Color(0xEEDDCC));
        imageLabel.setPreferredSize(new Dimension(80, 80));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setFont(UITheme.FONT_HEADING.deriveFont(Font.BOLD, 28f));
        imageLabel.setForeground(UITheme.PRIMARY);
        // Default text injection (Letter initials) shown until texture downloads
        imageLabel.setText(String.valueOf(item.getName().charAt(0)).toUpperCase());

        // Asynchronous texture download avoiding main UI thread lockups
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // Use loremflickr with a lock on the item ID for deterministic random food images
                java.net.URL url = java.net.URI.create("https://loremflickr.com/80/80/food?lock=" + item.getId()).toURL();
                java.awt.Image image = javax.imageio.ImageIO.read(url);
                if (image != null) {
                    return new ImageIcon(image);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageLabel.setIcon(icon); // Overwrite initials with successful payload
                        imageLabel.setText(null);
                    }
                } catch (Exception e) {
                    // Ignore, fallback handled gracefully via initials logic
                }
            }
        }.execute();

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(imageLabel, BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setOpaque(false);
        top.add(name, BorderLayout.CENTER);
        top.add(price, BorderLayout.EAST);
        midPanel.add(top, BorderLayout.NORTH);
        midPanel.add(desc, BorderLayout.CENTER);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(midPanel, BorderLayout.CENTER);
        card.add(addBtn, BorderLayout.EAST);

        // Required to ensure horizontal scrollbar logic functions effectively
        card.setMinimumSize(new Dimension(320, 125));
        card.setPreferredSize(new Dimension(320, 125));

        return card;
    }

    // ── Right: Cart panel ─────────────────────────────────────────
    /** Configures tabular representation reflecting mutable cart layout. */
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));

        JLabel lbl = UITheme.createLabel("Cart", UITheme.FONT_HEADING, UITheme.TEXT_DARK);

        // Cart associative table model locked down defensively against raw String edits
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

        // Footer: aggregation totals + resolution controls
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

    /** Mutator executing DB transaction associating item with table session. */
    private void addToCart(MenuItem item) {
        OrderController.addItemToOrder(currentOrder.getId(), item.getId(),
                item.getName(), item.getPrice(), 1);
        refreshCart(); // Hard reset UI syncing back cleanly from backend
    }

    /** Purges explicitly highlighted node structurally. */
    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }
        
        // Relies on identical ordering assumptions mapping visually to backend list index
        List<OrderItem> items = OrderController.getOrderItems(currentOrder.getId());
        if (row < items.size()) {
            OrderController.removeOrderItem(items.get(row).getId());
            refreshCart();
        }
    }

    /** 
     * Escalates current order status into finalized 'Bills', permanently terminating
     * the session workflow, optionally calculating and appending custom Discounts/Taxes.
     */
    private void generateBill() {
        List<OrderItem> items = OrderController.getOrderItems(currentOrder.getId());
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items before generating a bill.");
            return;
        }

        // Intercept workflow routing for optional percentage inputs via Dialog
        String discStr = JOptionPane.showInputDialog(this,
                "Enter Discount % (0 for none):", "Discount", JOptionPane.QUESTION_MESSAGE);
                
        if (discStr == null) return; // User opted to cancel/abort entirely
        
        double discPct = 0;
        try {
            discPct = Double.parseDouble(discStr);
        } catch (NumberFormatException ignored) {}
        discPct = Math.max(0, Math.min(100, discPct)); // Clamp bounds securely

        double taxPct = 5.0; // 5% flat GST constraint 
        Bill bill = BillController.generateBill(currentOrder.getId(), table.getId(), discPct, taxPct);

        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Error generating bill. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the receipt dialog parented strictly to the main dashboard 
        // (Avoiding attachment to this OrderPanel since we are destroying it imminently)
        Frame mainFrame = (Frame) SwingUtilities.getWindowAncestor(parent);
        BillDialog dialog = new BillDialog(mainFrame, bill, items, table.getNumber());

        // Tear down memory associated with Order window natively returning priority to Dashboard Grid
        SwingUtilities.getWindowAncestor(this).dispose();
        parent.refresh();

        // Reveal instantiated transactional receipt overlay
        dialog.setVisible(true);
    }

    /** Truncates and rebuilds table rendering querying strictly from SQL instance properties. */
    private void refreshCart() {
        cartModel.setRowCount(0); // Soft wipe rows
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
