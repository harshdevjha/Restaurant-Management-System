package com.restaurant.view;

import com.restaurant.controller.BillController;
import com.restaurant.model.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * BillHistoryPanel – Unrestricted historical log displaying finalized transactions.
 * Queries the database retrieving all recorded 'bills' sequentially.
 * Calculates an aggregate running-total summarizing application lifetime revenue interactively.
 * Exclusively accessible to users possessing the 'ADMIN' role designation.
 */
public class BillHistoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalRevenueLabel;

    public BillHistoryPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    /** Translates logical table structures and controls to physical Swing layout constructs. */
    private void initUI() {
        JLabel heading = UITheme.createLabel("🧾  Bill History", UITheme.FONT_TITLE, UITheme.PRIMARY);

        // ── Read-Only Transaction Log ─────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[] { "Bill #", "Order #", "Table", "Subtotal (₹)", "Disc (₹)", "GST (₹)", "Total (₹)",
                        "Date" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // Immutable ledger preventing accidental manual modification
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        // Constrain arbitrary ID fields mitigating unnecessary horizontal stretching
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDC8B8)));

        // ── Controls & Summary ────────────────────────────────────────
        JButton refreshBtn = UITheme.createButton("↻ Refresh", UITheme.ACCENT, Color.WHITE);
        refreshBtn.addActionListener(e -> loadData());

        totalRevenueLabel = UITheme.createLabel("Total Revenue: ₹ 0.00", UITheme.FONT_HEADING, UITheme.PRIMARY);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(totalRevenueLabel, BorderLayout.WEST);
        footer.add(refreshBtn, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.WEST);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    /** 
     * Erases currently rendered data and queries controller executing a bulk database scan.
     * Continuously interpolates 'Total Amount' simultaneously recalculating global revenue.
     */
    private void loadData() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        List<Bill> bills = BillController.getAllBills();
        double totalRevenue = 0;
        
        for (Bill b : bills) {
            tableModel.addRow(new Object[] {
                    b.getId(),
                    b.getOrderId(),
                    "T" + b.getTableNumber(),
                    String.format("%.2f", b.getSubtotal()),
                    String.format("%.2f", b.getDiscountAmt()),
                    String.format("%.2f", b.getTaxAmt()),
                    String.format("%.2f", b.getTotalAmount()),
                    b.getCreatedAt() != null ? sdf.format(b.getCreatedAt()) : "-"
            });
            // Aggregate raw monetary amount locally mitigating complex SQL SUM queries during general load
            totalRevenue += b.getTotalAmount();
        }
        totalRevenueLabel.setText(String.format("Total Revenue: ₹ %.2f", totalRevenue));
    }
}
