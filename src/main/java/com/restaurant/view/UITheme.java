package com.restaurant.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UITheme – centralised colour palette, fonts, and UI factory helpers
 * for the Restaurant Management System.
 */
public class UITheme {

    // ── Colour Palette (warm restaurant tones) ───────────────────
    public static final Color PRIMARY = new Color(0x8B1A1A); // Deep red
    public static final Color PRIMARY_DARK = new Color(0x5C1010); // Darker red
    public static final Color ACCENT = new Color(0xE87722); // Warm orange
    public static final Color SIDEBAR_BG = new Color(0x2C1A1A); // Very dark brown
    public static final Color SIDEBAR_HOVER = new Color(0x4A2C2C); // Hover brown
    public static final Color CONTENT_BG = new Color(0xFFF8F0); // Cream
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x1A1A1A);
    public static final Color TEXT_MUTED = new Color(0x777777);
    public static final Color SUCCESS = new Color(0x2E7D32); // Table FREE
    public static final Color DANGER = new Color(0xC62828); // Table OCCUPIED
    public static final Color WARNING = new Color(0xF57F17); // Pending

    // ── Font Stack ───────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_BTN = new Font("SansSerif", Font.BOLD, 13);

    /**
     * Creates a pill-shaped JButton with hover animation.
     *
     * @param text button label
     * @param bg   background colour
     * @param fg   foreground (text) colour
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));

        // Hover effect
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

    /** Creates a styled form text field. */
    public static JTextField createTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDC8B8), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    /** Creates a styled password field. */
    public static JPasswordField createPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDC8B8), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /** Creates a JLabel with style convenience. */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    /** Creates a sidebar nav JButton. */
    public static JButton createSidebarButton(String text) {
        JButton btn = new JButton("  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
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

    /** Applies common styling to a JTable. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(FONT_BTN);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0xFFE4CC));
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(0xEEE0D0));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    /** Creates a titled card panel (rounded white card). */
    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
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

    /** Sets consistent look and feel settings. Call once at startup. */
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
