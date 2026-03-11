package com.restaurant.view;

import com.restaurant.model.Bill;
import com.restaurant.model.OrderItem;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * BillDialog – formatted receipt dialog shown after a bill is generated.
 * Includes a print function via java.awt.print.
 */
public class BillDialog extends JDialog implements Printable {

    private final Bill bill;
    private final List<OrderItem> items;
    private final int tableNumber;

    public BillDialog(Frame parent, Bill bill, List<OrderItem> items, int tableNumber) {
        super(parent, "Bill Receipt — Table " + tableNumber, true);
        this.bill = bill;
        this.items = items;
        this.tableNumber = tableNumber;
        setSize(440, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // ── Receipt panel ────────────────────────────────────────
        JPanel receipt = new JPanel();
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));
        receipt.setBackground(Color.WHITE);
        receipt.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Header
        receipt.add(centeredLabel("🍽  RESTAURANT MANAGER",
                new Font("SansSerif", Font.BOLD, 16), UITheme.PRIMARY));
        receipt.add(centeredLabel("Fine Dining Experience",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        receipt.add(centeredLabel("Tel: +91 98765 43210  |  GST IN: 07ABCDE1234F1Z5",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        receipt.add(Box.createVerticalStrut(6));
        receipt.add(createDivider('─'));

        // Bill meta
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy  hh:mm a");
        String dateStr = sdf.format(bill.getCreatedAt() != null
                ? bill.getCreatedAt()
                : new Date());
        receipt.add(twoColLabel("Bill No:", "#" + bill.getId()));
        receipt.add(twoColLabel("Table:", "Table " + tableNumber));
        receipt.add(twoColLabel("Date:", dateStr));
        receipt.add(createDivider('─'));

        // Item rows
        receipt.add(headerRow("ITEM", "QTY", "RATE", "AMOUNT"));
        receipt.add(createDivider('-'));

        for (OrderItem oi : items) {
            receipt.add(itemRow(
                    oi.getMenuItemName(), oi.getQuantity(),
                    oi.getUnitPrice(), oi.getSubtotal()));
        }

        receipt.add(createDivider('─'));

        // Totals
        receipt.add(twoColLabel("Subtotal:", String.format("₹ %.2f", bill.getSubtotal())));
        if (bill.getDiscountPct() > 0) {
            receipt.add(twoColLabel(
                    String.format("Discount (%.0f%%):", bill.getDiscountPct()),
                    String.format("- ₹ %.2f", bill.getDiscountAmt())));
        }
        receipt.add(twoColLabel(
                String.format("GST (%.0f%%):", bill.getTaxPct()),
                String.format("₹ %.2f", bill.getTaxAmt())));
        receipt.add(createDivider('═'));

        JLabel totalLbl = centeredLabel(
                String.format("TOTAL:  ₹ %.2f", bill.getTotalAmount()),
                new Font("SansSerif", Font.BOLD, 15), UITheme.PRIMARY);
        receipt.add(totalLbl);
        receipt.add(Box.createVerticalStrut(10));
        receipt.add(centeredLabel("Thank you for dining with us! 🙏",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED));

        JScrollPane scroll = new JScrollPane(receipt,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnPanel.setBackground(UITheme.CONTENT_BG);

        JButton printBtn = UITheme.createButton("🖨  Print", UITheme.PRIMARY, Color.WHITE);
        printBtn.addActionListener(e -> doPrint());

        JButton closeBtn = UITheme.createButton("Close", UITheme.ACCENT, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ── Print support ─────────────────────────────────────────────
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0)
            return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        double scale = pageFormat.getImageableWidth() / getWidth();
        g2.scale(scale, scale);
        printAll(g2);
        return PAGE_EXISTS;
    }

    private void doPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Receipt building helpers ──────────────────────────────────

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height + 4));
        return lbl;
    }

    private JPanel twoColLabel(String left, String right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l = new JLabel(left);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        JLabel r = new JLabel(right);
        r.setFont(UITheme.FONT_BTN);
        r.setForeground(UITheme.TEXT_DARK);
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(l, BorderLayout.WEST);
        row.add(r, BorderLayout.EAST);
        return row;
    }

    private JPanel headerRow(String c1, String c2, String c3, String c4) {
        return buildItemRow(c1, c2, c3, c4, UITheme.FONT_BTN, UITheme.TEXT_DARK);
    }

    private JPanel itemRow(String name, int qty, double price, double sub) {
        return buildItemRow(
                name, String.valueOf(qty),
                String.format("%.2f", price),
                String.format("%.2f", sub),
                UITheme.FONT_SMALL, UITheme.TEXT_DARK);
    }

    private JPanel buildItemRow(String c1, String c2, String c3, String c4, Font f, Color fg) {
        JPanel row = new JPanel(new GridLayout(1, 4));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        for (String text : new String[] { c1, c2, c3, c4 }) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(f);
            lbl.setForeground(fg);
            row.add(lbl);
        }
        return row;
    }

    private JSeparator createDivider(char ch) {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xDDC8B8));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        return sep;
    }
}
