package util;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;

public class ThemeManager {

    // ── Mode flag ─────────────────────────────────────────────────
    public static boolean isDark = true;

    // ── Dark Mode Palette ─────────────────────────────────────────
    private static final Color D_BG_PRIMARY    = new Color(13,  13,  13);
    private static final Color D_BG_SECONDARY  = new Color(22,  22,  22);
    private static final Color D_BG_TERTIARY   = new Color(32,  32,  32);
    private static final Color D_BG_HOVER      = new Color(42,  42,  42);
    private static final Color D_TEXT_PRIMARY   = new Color(240, 240, 240);
    private static final Color D_TEXT_SECONDARY = new Color(160, 160, 160);
    private static final Color D_TEXT_MUTED     = new Color(90,  90,  90);
    private static final Color D_BORDER         = new Color(40,  40,  40);
    private static final Color D_TABLE_HEADER   = new Color(18,  18,  18);
    private static final Color D_TABLE_ROW_ODD  = new Color(20,  20,  20);
    private static final Color D_TABLE_ROW_EVEN = new Color(26,  26,  26);

    // ── Light Mode Palette ────────────────────────────────────────
    private static final Color L_BG_PRIMARY    = new Color(233, 235, 238);
    private static final Color L_BG_SECONDARY  = new Color(243, 244, 246);
    private static final Color L_BG_TERTIARY   = new Color(220, 222, 226);
    private static final Color L_BG_HOVER      = new Color(208, 210, 215);
    private static final Color L_TEXT_PRIMARY   = new Color(17,  24,  39);
    private static final Color L_TEXT_SECONDARY = new Color(75,  85,  99);
    private static final Color L_TEXT_MUTED     = new Color(120, 128, 143);
    private static final Color L_BORDER         = new Color(196, 200, 208);
    private static final Color L_TABLE_HEADER   = new Color(220, 222, 227);
    private static final Color L_TABLE_ROW_ODD  = new Color(233, 235, 238);
    private static final Color L_TABLE_ROW_EVEN = new Color(243, 244, 246);

    // ── Active Colors (dipakai semua panel) ───────────────────────
    public static Color BG_PRIMARY    = D_BG_PRIMARY;
    public static Color BG_SECONDARY  = D_BG_SECONDARY;
    public static Color BG_TERTIARY   = D_BG_TERTIARY;
    public static Color BG_HOVER      = D_BG_HOVER;
    public static Color TEXT_PRIMARY   = D_TEXT_PRIMARY;
    public static Color TEXT_SECONDARY = D_TEXT_SECONDARY;
    public static Color TEXT_MUTED     = D_TEXT_MUTED;
    public static Color BORDER         = D_BORDER;
    public static Color TABLE_HEADER   = D_TABLE_HEADER;
    public static Color TABLE_ROW_ODD  = D_TABLE_ROW_ODD;
    public static Color TABLE_ROW_EVEN = D_TABLE_ROW_EVEN;

    // ── Accent & Fixed (sama di kedua mode) ───────────────────────
    public static final Color ACCENT        = new Color(249, 115, 22);
    public static final Color ACCENT_HOVER  = new Color(234, 88,  12);
    public static final Color ACCENT_GREEN  = new Color(34,  197, 94);
    public static final Color ACCENT_AMBER  = new Color(234, 179, 8);
    public static final Color ACCENT_RED    = new Color(239, 68,  68);
    public static final Color ACCENT_BLUE   = new Color(59,  130, 246);
    public static final Color BORDER_FOCUS  = new Color(249, 115, 22);
    public static final Color TABLE_SELECT  = new Color(249, 115, 22, 50);

    // ── Font (sama di kedua mode) ─────────────────────────────────
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,   15);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN,  11);

    // ── Toggle Methods ────────────────────────────────────────────
    public static void setDarkMode() {
        isDark = true;
        BG_PRIMARY = D_BG_PRIMARY;       BG_SECONDARY  = D_BG_SECONDARY;
        BG_TERTIARY = D_BG_TERTIARY;     BG_HOVER      = D_BG_HOVER;
        TEXT_PRIMARY = D_TEXT_PRIMARY;   TEXT_SECONDARY = D_TEXT_SECONDARY;
        TEXT_MUTED = D_TEXT_MUTED;       BORDER         = D_BORDER;
        TABLE_HEADER = D_TABLE_HEADER;   TABLE_ROW_ODD  = D_TABLE_ROW_ODD;
        TABLE_ROW_EVEN = D_TABLE_ROW_EVEN;
    }

    public static void setLightMode() {
        isDark = false;
        BG_PRIMARY = L_BG_PRIMARY;       BG_SECONDARY  = L_BG_SECONDARY;
        BG_TERTIARY = L_BG_TERTIARY;     BG_HOVER      = L_BG_HOVER;
        TEXT_PRIMARY = L_TEXT_PRIMARY;   TEXT_SECONDARY = L_TEXT_SECONDARY;
        TEXT_MUTED = L_TEXT_MUTED;       BORDER         = L_BORDER;
        TABLE_HEADER = L_TABLE_HEADER;   TABLE_ROW_ODD  = L_TABLE_ROW_ODD;
        TABLE_ROW_EVEN = L_TABLE_ROW_EVEN;
    }

    public static void toggleTheme() {
        if (isDark) setLightMode(); else setDarkMode();
    }
    // ── Helper: tombol styled ──────────────────────────────
    public static javax.swing.JButton makeButton(String text, Color bg) {
        javax.swing.JButton btn = new javax.swing.JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        Color hover = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // ── Helper: styling JTable ─────────────────────────────
    public static void styleTable(javax.swing.JTable table) {
        table.setBackground(TABLE_ROW_ODD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(new Color(249, 115, 22));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        javax.swing.JTable t, Object val, boolean isSel,
                        boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                    if (!isSel)
                        setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
                    setForeground(TEXT_PRIMARY);
                    setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    return this;
                }
            });
        // Daftarkan renderer yang SAMA untuk tipe angka & tanggal juga,
        // supaya kolom ber-tipe Integer/Double/Date (dipakai untuk sorting numerik
        // yang benar) tidak balik memakai renderer bawaan Java (rata kanan +
        // border fokus biru bawaan Look & Feel).
        javax.swing.table.TableCellRenderer sharedRenderer = table.getDefaultRenderer(Object.class);
        table.setDefaultRenderer(Integer.class, sharedRenderer);
        table.setDefaultRenderer(Double.class, sharedRenderer);
        table.setDefaultRenderer(java.sql.Date.class, sharedRenderer);
    }

    // ── Helper: styling JTextField ─────────────────────────
    public static void styleField(javax.swing.JTextField field) {
        field.setBackground(BG_TERTIARY);
        field.setForeground(TEXT_PRIMARY);
        field.setFont(FONT_REGULAR);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER),
            new javax.swing.border.EmptyBorder(8, 12, 8, 12)
        ));
    }

    // ── Helper: styling JScrollPane ────────────────────────
    public static void styleScrollPane(javax.swing.JScrollPane sp) {
        sp.getViewport().setBackground(BG_SECONDARY);
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER));
        sp.getVerticalScrollBar().setBackground(BG_SECONDARY);
        sp.getHorizontalScrollBar().setBackground(BG_SECONDARY);
    }
    
    public static void styleComboBox(javax.swing.JComboBox<?> cb) {
    // Ganti WindowsComboBoxUI dengan BasicComboBoxUI supaya background
    // area display bisa kita kontrol sendiri lewat paintCurrentValueBackground
    cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
        @Override
        public void paintCurrentValueBackground(java.awt.Graphics g,
                java.awt.Rectangle bounds, boolean hasFocus) {
            g.setColor(BG_SECONDARY);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    });
    cb.setBackground(BG_SECONDARY);
    cb.setForeground(TEXT_PRIMARY);
    cb.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER));
    cb.setRenderer(new javax.swing.DefaultListCellRenderer() {
        @Override
        public java.awt.Component getListCellRendererComponent(
                javax.swing.JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setOpaque(true);
            if (index == -1) {
                setBackground(BG_SECONDARY);
                setForeground(TEXT_PRIMARY);
            } else if (isSelected) {
                setBackground(ACCENT);
                setForeground(Color.WHITE);
            } else {
                setBackground(BG_SECONDARY);
                setForeground(TEXT_PRIMARY);
            }
            setBorder(new javax.swing.border.EmptyBorder(4, 8, 4, 8));
            return this;
        }
    });
}

    // ── Helper: serap fokus saat klik area kosong (background panel) ──
    // Supaya field yang sedang fokus (misal kotak search) berhenti
    // berkedip saat user klik ke area kosong yang gak punya komponen apapun.
    public static void enableClickAwayFocus(javax.swing.JComponent panel) {
        panel.setFocusable(true);
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }
}