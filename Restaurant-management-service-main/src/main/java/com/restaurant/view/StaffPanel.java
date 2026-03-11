package com.restaurant.view;

import com.restaurant.controller.AdminController;
import com.restaurant.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * StaffPanel – Admin panel to manage staff / admin accounts.
 */
public class StaffPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public StaffPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JLabel heading = UITheme.createLabel("👥  Staff Management", UITheme.FONT_TITLE, UITheme.PRIMARY);

        tableModel = new DefaultTableModel(
                new String[] { "ID", "Username", "Full Name", "Role", "Active" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDC8B8)));

        JButton addBtn = UITheme.createButton("+ Add Staff", UITheme.SUCCESS, Color.WHITE);
        JButton editBtn = UITheme.createButton("✎ Edit", UITheme.PRIMARY, Color.WHITE);
        JButton deleteBtn = UITheme.createButton("✕ Delete", UITheme.DANGER, Color.WHITE);
        JButton refreshBtn = UITheme.createButton("↻ Refresh", UITheme.ACCENT, Color.WHITE);

        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        add(heading, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (User u : AdminController.getAllStaff()) {
            tableModel.addRow(new Object[] {
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                    u.isActive() ? "Yes" : "No"
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        AdminController.getAllStaff().stream().filter(u -> u.getId() == id)
                .findFirst().ifPresent(this::showDialog);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user '" + tableModel.getValueAt(row, 2) + "'?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (AdminController.deleteUser(id))
                loadData();
        }
    }

    private void showDialog(User existing) {
        boolean isNew = (existing == null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Add Staff" : "Edit Staff", true);
        dialog.setSize(380, 310);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.setBackground(UITheme.CONTENT_BG);

        JTextField unField = UITheme.createTextField(15);
        JTextField passField = UITheme.createTextField(15);
        JTextField nameField = UITheme.createTextField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "STAFF", "ADMIN" });
        JCheckBox activeBox = new JCheckBox("Active", true);

        if (!isNew) {
            unField.setText(existing.getUsername());
            passField.setText(existing.getPassword());
            nameField.setText(existing.getFullName());
            roleCombo.setSelectedItem(existing.getRole());
            activeBox.setSelected(existing.isActive());
        }

        form.add(UITheme.createLabel("Username:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        form.add(unField);
        form.add(UITheme.createLabel("Password:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        form.add(passField);
        form.add(UITheme.createLabel("Full Name:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        form.add(nameField);
        form.add(UITheme.createLabel("Role:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        form.add(roleCombo);
        form.add(UITheme.createLabel("Status:", UITheme.FONT_BTN, UITheme.TEXT_DARK));
        form.add(activeBox);

        JButton save = UITheme.createButton("Save", UITheme.SUCCESS, Color.WHITE);
        JButton cancel = UITheme.createButton("Cancel", UITheme.DANGER, Color.WHITE);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            User u = isNew ? new User() : existing;
            u.setUsername(unField.getText().trim());
            u.setPassword(passField.getText().trim());
            u.setFullName(nameField.getText().trim());
            u.setRole((String) roleCombo.getSelectedItem());
            u.setActive(activeBox.isSelected());
            boolean ok = isNew ? AdminController.addUser(u) : AdminController.updateUser(u);
            if (ok) {
                loadData();
                dialog.dispose();
            } else
                JOptionPane.showMessageDialog(dialog, "Save failed.", "Error", JOptionPane.ERROR_MESSAGE);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.CONTENT_BG);
        btnRow.add(cancel);
        btnRow.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
