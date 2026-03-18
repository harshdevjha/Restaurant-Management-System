package com.restaurant.view;

import com.restaurant.controller.ReportController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * ReportsPanel – Tactical financial overview interface abstracting aggregation reporting.
 * Exclusively leverages complex backend grouping queries translating them
 * visually into concise Daily/Monthly summary formats seamlessly.
 * Accessible securely only by 'ADMIN' roles.
 */
public class ReportsPanel extends JPanel {

    private JSpinner daySpinner, monthSpinner, yearSpinner;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
    }

    /** Structures the date input mechanisms natively querying calendar systems to bootstrap state. */
    private void initUI() {
        JLabel heading = UITheme.createLabel("📊  Sales Reports", UITheme.FONT_TITLE, UITheme.PRIMARY);

        // ── Input Controls Bar ─────────────────────────────────────────
        // Harvest operational system time guaranteeing inputs logically map to today
        Calendar now = Calendar.getInstance();
        daySpinner = new JSpinner(new SpinnerNumberModel(now.get(Calendar.DAY_OF_MONTH), 1, 31, 1));
        monthSpinner = new JSpinner(new SpinnerNumberModel(now.get(Calendar.MONTH) + 1, 1, 12, 1));
        yearSpinner = new JSpinner(new SpinnerNumberModel(now.get(Calendar.YEAR), 2020, 2099, 1));

        daySpinner.setPreferredSize(new Dimension(60, 30));
        monthSpinner.setPreferredSize(new Dimension(60, 30));
        yearSpinner.setPreferredSize(new Dimension(80, 30));
        
        // Suppress default localized formatting preventing random comma artifacts in integer years
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "####"));

        JButton dailyBtn = UITheme.createButton("Daily Report", UITheme.PRIMARY, Color.WHITE);
        JButton monthlyBtn = UITheme.createButton("Monthly Report", UITheme.ACCENT, Color.WHITE);

        dailyBtn.addActionListener(e -> showDailyReport());
        monthlyBtn.addActionListener(e -> showMonthlyReport());

        // FlowLayout strictly pushing elements closely leftwards avoiding spread
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(UITheme.createLabel("Day:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        controls.add(daySpinner);
        controls.add(UITheme.createLabel("Month:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        controls.add(monthSpinner);
        controls.add(UITheme.createLabel("Year:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        controls.add(yearSpinner);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(dailyBtn);
        controls.add(monthlyBtn);

        // ── Report Table ────────────────────────────────────────────────
        // Structurally agnostic rows accommodating both Single-Day singular entries AND Multi-Day grouped logs
        tableModel = new DefaultTableModel(new String[] { "Date", "Bills Issued", "Revenue (₹)" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDC8B8)));

        summaryLabel = UITheme.createLabel("", UITheme.FONT_HEADING, UITheme.PRIMARY);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(controls, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    /** 
     * Extracts exact day target parsing through Spinners and executing a synchronous targeted SUM query.
     */
    private void showDailyReport() {
        tableModel.setRowCount(0);
        Calendar cal = buildCalendar(); // Coerce primitive ints securely into Date target
        
        Map<String, Object> result = ReportController.getDailyReport(cal.getTime());
        // Defensively assert payload data structurally
        int bills = result.containsKey("bill_count") ? ((Number) result.get("bill_count")).intValue() : 0;
        double rev = result.containsKey("revenue") ? ((Number) result.get("revenue")).doubleValue() : 0;
        
        tableModel.addRow(new Object[] { result.get("date"), bills, String.format("%.2f", rev) });
        summaryLabel.setText(String.format("Revenue on %s:  ₹ %.2f", result.get("date"), rev));
    }

    /** 
     * Extracts broad Month scale resolving grouped SQL aggregation displaying 
     * daily breakdowns across an entire cyclical month block.
     */
    private void showMonthlyReport() {
        tableModel.setRowCount(0);
        int month = (int) monthSpinner.getValue();
        int year = (int) yearSpinner.getValue();
        
        // Receive tabular array containing grouped rows mapping 1:1 against discrete days experiencing sales
        List<Map<String, Object>> rows = ReportController.getMonthlyReport(month, year);
        for (Map<String, Object> row : rows) {
            double rev = ((Number) row.get("revenue")).doubleValue();
            tableModel.addRow(new Object[] {
                    row.get("date"), row.get("bill_count"), String.format("%.2f", rev)
            });
        }
        
        // Execute secondary isolated SUM query acquiring raw unified month total avoiding complex row-math
        double monthTotal = ReportController.getTotalMonthlyRevenue(month, year);
        summaryLabel
                .setText(String.format("Total for %d/%d:  ₹ %.2f  (%d days)", month, year, monthTotal, rows.size()));
    }

    /** Resolves JSpinner disparate components structurally into standard Date representation avoiding time shifting */
    private Calendar buildCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, (int) daySpinner.getValue());
        cal.set(Calendar.MONTH, (int) monthSpinner.getValue() - 1); // Compensate Calendar 0-Index rules
        cal.set(Calendar.YEAR, (int) yearSpinner.getValue());
        return cal;
    }
}
