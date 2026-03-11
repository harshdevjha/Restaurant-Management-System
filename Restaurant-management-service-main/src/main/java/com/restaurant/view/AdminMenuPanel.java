package com.restaurant.view;

import com.restaurant.controller.AdminController;
import com.restaurant.controller.MenuController;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminMenuPanel – Admin view to Add / Edit / Delete menu items.
 */
public class AdminMenuPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;

    public AdminMenuPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JLabel heading = UITheme.createLabel("🍕  Menu Management", UITheme.FONT_TITLE, UITheme.PRIMARY);

        // Table
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Category", "Name", "Description", "Price (₹)", "Available"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDC8B8)));

        // Toolbar
        JButton addBtn    = UITheme.createButton("+ Add Item",    UITheme.SUCCESS,  Color.WHITE);
        JButton editBtn   = UITheme.createButton("✎ Edit Item",   UITheme.PRIMARY,  Color.WHITE);
        JButton deleteBtn = UITheme.createButton("✕ Delete Item", UITheme.DANGER,   Color.WHITE);
        JButton refreshBtn= UITheme.createButton("↻ Refresh",     UITheme.ACCENT,   Color.WHITE);

        addBtn.addActionListener(e    -> showItemDialog(null));
        editBtn.addActionListener(e   -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e-> loadData());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn); toolbar.add(refreshBtn);

        add(heading, BorderLayout.NORTH);
        add(toolbar, BorderLayout.SOUTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (MenuItem mi : MenuController.getAllItems()) {
            tableModel.addRow(new Object[]{
                mi.getId(), mi.getCategoryName(), mi.getName(),
                mi.getDescription(), String.format("%.2f", mi.getPrice()),
                mi.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to edit."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        // Find the item from DB
        List<MenuItem> all = MenuController.getAllItems();
        all.stream().filter(mi -> mi.getId() == id).findFirst().ifPresent(this::showItemDialog);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete '" + tableModel.getValueAt(row, 2) + "'?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (MenuController.deleteItem(id)) loadData();
            else JOptionPane.showMessageDialog(this, "Could not delete item.", "Error",
                                                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showItemDialog(MenuItem existing) {
        boolean isNew = (existing == null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                      isNew ? "Add Menu Item" : "Edit Menu Item", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Form
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.setBackground(UITheme.CONTENT_BG);

        // Category combo
        List<Category> cats = MenuController.getAllCategories();
        JComboBox<Category> catCombo = new JComboBox<>(cats.toArray(new Category[0]));

        JTextField nameField = UITheme.createTextField(15);
        JTextField descField = UITheme.createTextField(15);
        JTextField priceField= UITheme.createTextField(10);
        JCheckBox  availBox  = new JCheckBox("Available", true);

        if (!isNew) {
            cats.stream().filter(c -> c.getId() == existing.getCategoryId())
                         .findFirst().ifPresent(catCombo::setSelectedItem);
            nameField.setText(existing.getName());
            descField.setText(existing.getDescription());
            priceField.setText(String.format("%.2f", existing.getPrice()));
            availBox.setSelected(existing.isAvailable());
        }

        form.add(UITheme.createLabel("Category:", UITheme.FONT_BTN, UITheme.TEXT_DARK)); form.add(catCombo);
        form.add(UITheme.createLabel("Name:",     UITheme.FONT_BTN, UITheme.TEXT_DARK)); form.add(nameField);
        form.add(UITheme.createLabel("Description:", UITheme.FONT_BTN, UITheme.TEXT_DARK)); form.add(descField);
        form.add(UITheme.createLabel("Price (₹):", UITheme.FONT_BTN, UITheme.TEXT_DARK)); form.add(priceField);
        form.add(UITheme.createLabel("Status:",   UITheme.FONT_BTN, UITheme.TEXT_DARK)); form.add(availBox);

        JButton save = UITheme.createButton("Save", UITheme.SUCCESS, Color.WHITE);
        JButton cancel = UITheme.createButton("Cancel", UITheme.DANGER, Color.WHITE);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                MenuItem mi = isNew ? new MenuItem() : existing;
                Category selCat = (Category) catCombo.getSelectedItem();
                mi.setCategoryId(selCat != null ? selCat.getId() : 0);
                mi.setName(nameField.getText().trim());
                mi.setDescription(descField.getText().trim());
                mi.setPrice(Double.parseDouble(priceField.getText().trim()));
                mi.setAvailable(availBox.isSelected());

                boolean ok = isNew ? MenuController.addItem(mi) : MenuController.updateItem(mi);
                if (ok) { loadData(); dialog.dispose(); }
                else JOptionPane.showMessageDialog(dialog, "Save failed.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a valid number.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.CONTENT_BG);
        btnRow.add(cancel); btnRow.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
