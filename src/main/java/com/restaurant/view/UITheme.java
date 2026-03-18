package com.restaurant.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UITheme – Centralized design system, color palette, typography definitions, 
 * and UI component factory helpers for the Restaurant Management System.
 * Ensures visual consistency across all forms, dialogs, and panels.
 */
public class UITheme {

    // ── Color Palette (warm restaurant tones) ───────────────────
    public static final Color PRIMARY = new Color(0x8B1A1A); // Deep red (Headers, Primary Buttons)
    public static final Color PRIMARY_DARK = new Color(0x5C1010); // Darker red (Hover states, Background Gradients)
    public static final Color ACCENT = new Color(0xE87722); // Warm orange (Highlighting, Logout Buttons)
    public static final Color SIDEBAR_BG = new Color(0x2C1A1A); // Very dark brown (Navigation Background)
    public static final Color SIDEBAR_HOVER = new Color(0x4A2C2C); // Hover brown (Navigation Hover)
    public static final Color CONTENT_BG = new Color(0xFFF8F0); // Cream (Main Content Area Background)
    public static final Color CARD_BG = Color.WHITE; // White (Panel/Card Backgrounds)
    public static final Color TEXT_DARK = new Color(0x1A1A1A); // Near Black (Standard Text)
    public static final Color TEXT_MUTED = new Color(0x777777); // Grey (Subtitle/Placeholder Text)
    public static final Color SUCCESS = new Color(0x2E7D32); // Deep Green (Table FREE status)
    public static final Color DANGER = new Color(0xC62828); // Bright Red (Table OCCUPIED status, Error Messages)
    public static final Color WARNING = new Color(0xF57F17); // Amber (Pending Actions)

    // ── Font Stack ───────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22); // Screen Titles
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16); // Section Headers
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13); // Standard Content
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11); // Footnotes, Labels
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12); // Receipts, Tabular Numbers
    public static final Font FONT_BTN = new Font("SansSerif", Font.BOLD, 13); // Button Text

    /**
     * Factory method creating a modern, pill-shaped JButton with integrated hover animation.
     * Uses Java2D anti-aliasing for smooth rounded corners.
     *
     * @param text The display label of the button.
     * @param bg   The primary background color.
     * @param fg   The foreground (text) color.
     * @return A fully styled, interaction-ready JButton component.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Enable anti-aliasing for smooth borders
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                // Draw rounded rectangle with 14px corner radius
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g); // Paint standard text on top
            }
        };
        // Reset default Swing visual properties
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Pad the width to accommodate pill shape
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));

        // Interaction: Darken background dynamically on mouse hover
        Color hoverBg = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.repaint();
            }
        });
        return btn;
    }

    /** 
     * Factory method creating a styled, single-line text input field.
     * Includes padding and custom border color matching the theme.
     * @param cols Approximate character width of the field.
     */
    public static JTextField createTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        // Compound border provides 1px solid stroke + internal padding space
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDC8B8), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    /** 
     * Factory method creating a visually masked password input field.
     * Styling matches {@link #createTextField(int)}.
     */
    public static JPasswordField createPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDC8B8), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /** 
     * Convenience factory for creating a statically styled text label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    /** 
     * Factory method creating specialized navigation buttons mapped for the application sidebar.
     * Features wider, left-aligned layout with active-state coloring.
     */
    public static JButton createSidebarButton(String text) {
        JButton btn = new JButton("  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                // Margin inside bounds intentionally left for visual breathing room
                g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Interaction listener for sidebar navigation list
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(SIDEBAR_HOVER);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
                btn.repaint();
            }
        });
        return btn;
    }

    /** 
     * Mutator utility structurally resetting the default JTable appearance 
     * to match the application's clean, spaced grid esthetic.
     * @param table Target Swing JTable instance object.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30); // Vertically space data comfortably
        table.getTableHeader().setFont(FONT_BTN);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0xFFE4CC)); // Peach highlight row
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(0xEEE0D0));
        table.setShowGrid(true);
        // Minimize intrinsic cell borders
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    /** 
     * Creates a modular visual container (a card) rendering dropping a subtle 
     * rounded white background. Used to group settings or fields logically.
     * @param title Appended visually at the top-left of the enclosed space, if provided.
     */
    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = createLabel(title, FONT_HEADING, PRIMARY);
            card.add(lbl, BorderLayout.NORTH);
        }
        return card;
    }

    /** 
     * Globally overrides Java UIManager keys. Injects theme constraints deeply 
     * into native dialogs (JOptionPane) and core fallback components.
     * Should be executed strictly once during Main bootstrap sequence.
     */
    public static void applyDefaults() {
        UIManager.put("Panel.background", CONTENT_BG);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BTN);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BTN);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
    }
}
