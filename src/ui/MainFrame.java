package ui;

import util.Session;
import util.ThemeManager;
import util.Refreshable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane tabs;
    private static Point lastLocation = null;
    private static int lastTabIndex = 0;

    public MainFrame() {
        setTitle("Toko Berkah Jaya – Sistem Penjualan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 800);
        if (lastLocation != null) setLocation(lastLocation);
        else setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeManager.BG_PRIMARY);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeManager.BG_PRIMARY);

        // ── Top Bar ───────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeManager.BG_PRIMARY);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER),
            new EmptyBorder(12, 28, 12, 28)
        ));

        // Logo kiri
        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBar.setOpaque(false);

        // Kotak oranye kecil sebagai logo
        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(249, 115, 22),
                    getWidth(), getHeight(), new Color(239, 68, 68));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        logoBox.setPreferredSize(new Dimension(28, 28));
        logoBox.setOpaque(false);

        JLabel lblApp = new JLabel("TOKO BERKAH JAYA");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblApp.setForeground(ThemeManager.TEXT_PRIMARY);

        leftBar.add(logoBox);
        leftBar.add(lblApp);

        // Kanan: user info + logout
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBar.setOpaque(false);

        JLabel lblUser = new JLabel(Session.namaLengkap);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(160, 160, 160));

        // Badge level
        JLabel lblLevel = new JLabel(Session.level) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(249, 115, 22, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeManager.ACCENT);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLevel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLevel.setForeground(ThemeManager.ACCENT);
        lblLevel.setBorder(new EmptyBorder(4, 10, 4, 10));
        lblLevel.setOpaque(false);

        JButton btnLogout = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(ThemeManager.TEXT_PRIMARY);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setOpaque(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(6, 16, 6, 16));

        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(220, 40, 40));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(239, 68, 68));
            }
        });

        btnLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        rightBar.add(lblUser);
        rightBar.add(lblLevel);
        JButton btnToggleTheme = new JButton(ThemeManager.isDark ? "☀" : "🌙");
        btnToggleTheme.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btnToggleTheme.setForeground(ThemeManager.TEXT_PRIMARY);
        btnToggleTheme.setFocusPainted(false);
        btnToggleTheme.setBorderPainted(false);
        btnToggleTheme.setContentAreaFilled(false);
        btnToggleTheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleTheme.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnToggleTheme.setToolTipText(ThemeManager.isDark ? "Mode Terang" : "Mode Gelap");
        btnToggleTheme.addActionListener(e -> {
            lastLocation = getLocation();
            lastTabIndex = tabs.getSelectedIndex();
            ThemeManager.toggleTheme();
            dispose();
            javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        });
        rightBar.add(btnToggleTheme);
        rightBar.add(btnLogout);

        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        // ── Tabs ──────────────────────────────────────────
        tabs = new JTabbedPane();
        tabs.setBackground(ThemeManager.BG_PRIMARY);
        tabs.setForeground(ThemeManager.TEXT_SECONDARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.setUI(new BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight      = ThemeManager.BG_PRIMARY;
                lightHighlight = ThemeManager.BG_PRIMARY;
                shadow         = ThemeManager.BORDER;
                darkShadow     = ThemeManager.BORDER;
                focus          = ThemeManager.ACCENT;
            }
            @Override protected int calculateTabHeight(int tp, int ti, int fh) {
                return 44;
            }
            @Override protected void paintTabBackground(Graphics g, int tp,
                    int ti, int x, int y, int w, int h, boolean isSel) {
                g.setColor(isSel ? ThemeManager.BG_TERTIARY : ThemeManager.BG_PRIMARY);
                g.fillRect(x, y, w, h);
            }
            @Override protected void paintTabBorder(Graphics g, int tp,
                    int ti, int x, int y, int w, int h, boolean isSel) {
                if (isSel) {
                    Graphics2D g2 = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(
                        x, 0, new Color(249, 115, 22),
                        x + w, 0, new Color(239, 68, 68));
                    g2.setPaint(gp);
                    g2.fillRect(x, y + h - 3, w, 3);
                }
            }
            @Override protected void paintFocusIndicator(Graphics g, int tp,
                    Rectangle[] r, int ti, Rectangle iconRect,
                    Rectangle textRect, boolean isSelected) {}
            @Override protected void paintContentBorder(Graphics g, int tp, int si) {
                g.setColor(ThemeManager.BORDER);
                g.fillRect(0,
                    calculateTabAreaHeight(tp, runCount, maxTabHeight) - 1,
                    tabPane.getWidth(), 1);
            }
        });
        
            // Dashboard untuk SEMUA role
            tabs.addTab("  Dashboard  ", new DashboardPanel());

        // Tab khusus ADMIN — master data user & kategori
        if ("Admin".equals(Session.level)) {
            tabs.addTab("  User  ",     new UserPanel());
            tabs.addTab("  Kategori  ", new KategoriPanel());
        }

            // Tab untuk SEMUA role
            tabs.addTab("  Barang  ",    new BarangPanel());
            tabs.addTab("  Customer  ",  new CustomerPanel());
            tabs.addTab("  Transaksi  ", new TransaksiPanel());

        // Tab khusus ADMIN — laporan
        if ("Admin".equals(Session.level)) {
            tabs.addTab("  Laporan  ",  new LaporanPanel());
        }

        // Restore tab yang aktif sebelum toggle tema
        if (lastTabIndex > 0 && lastTabIndex < tabs.getTabCount()) {
            tabs.setSelectedIndex(lastTabIndex);
        }

        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable) {
                ((Refreshable) selected).refreshData();
            }
        });
        
        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        add(root);
    }
}