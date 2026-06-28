package ui;

import koneksi.Koneksi;
import util.Session;
import util.ThemeManager;
import util.Refreshable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel implements Refreshable {

    private JLabel lblTotalTransaksi, lblPendapatanHariIni;
    private JLabel lblTotalBarang, lblStokMenipis;
    private JLabel lblTotalCustomer, lblWelcome, lblTanggal;
    private JPanel tabelStokPanel;

    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(
        new java.util.Locale("id", "ID"));

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(ThemeManager.BG_PRIMARY);
        main.setBorder(new EmptyBorder(32, 32, 32, 32));

        // ── Header ────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 28, 0));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        lblWelcome = new JLabel("Selamat datang, " + Session.namaLengkap + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(ThemeManager.TEXT_PRIMARY);

        lblTanggal = new JLabel(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
                new java.util.Locale("id", "ID"))));
        lblTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTanggal.setForeground(ThemeManager.TEXT_SECONDARY);

        headerLeft.add(lblWelcome);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblTanggal);

        JButton btnRefresh = new JButton("Refresh") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_TERTIARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRefresh.setForeground(ThemeManager.ACCENT);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setOpaque(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(249, 115, 22, 60)),
            new EmptyBorder(8, 20, 8, 20)
        ));
        btnRefresh.addActionListener(e -> loadData());

        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(btnRefresh);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(refreshWrap, BorderLayout.EAST);

        // ── Stat Cards (baris 1) ──────────────────────────
        JPanel row1 = new JPanel(new GridLayout(1, 3, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card transaksi hari ini
        JPanel cardTransaksi = makeStatCard(
            "TRANSAKSI HARI INI",
            "0",
            "Total faktur hari ini",
            new Color(99, 102, 241)
        );
        lblTotalTransaksi = findValueLabel(cardTransaksi);

        // Card pendapatan
        JPanel cardPendapatan = makeStatCard(
            "PENDAPATAN HARI INI",
            "Rp 0",
            "Total dari semua transaksi",
            ThemeManager.ACCENT
        );
        lblPendapatanHariIni = findValueLabel(cardPendapatan);

        // Card customer
        JPanel cardCustomer = makeStatCard(
            "TOTAL CUSTOMER",
            "0",
            "Pelanggan terdaftar",
            new Color(34, 197, 94)
        );
        lblTotalCustomer = findValueLabel(cardCustomer);

        row1.add(cardTransaksi);
        row1.add(cardPendapatan);
        row1.add(cardCustomer);

        // ── Stat Cards (baris 2) ──────────────────────────
        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card total barang
        JPanel cardBarang = makeStatCard(
            "TOTAL BARANG",
            "0",
            "Jenis produk tersedia",
            new Color(168, 85, 247)
        );
        lblTotalBarang = findValueLabel(cardBarang);

        // Card stok menipis
        JPanel cardStok = makeStatCard(
            "STOK MENIPIS",
            "0",
            "Barang dengan stok < 10",
            new Color(239, 68, 68)
        );
        lblStokMenipis = findValueLabel(cardStok);

        // Card level user
        JPanel cardUser = makeStatCard(
            "LOGIN SEBAGAI",
            Session.level,
            Session.namaLengkap,
            new Color(234, 179, 8)
        );

        row2.add(cardBarang);
        row2.add(cardStok);
        row2.add(cardUser);

        // ── Tabel Stok Menipis ────────────────────────────
        JPanel tabelSection = new JPanel(new BorderLayout());
        tabelSection.setOpaque(false);
        tabelSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabelSection.setBorder(new EmptyBorder(24, 0, 0, 0));

        JPanel tabelHeader = new JPanel(new BorderLayout());
        tabelHeader.setOpaque(false);
        tabelHeader.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel tabelTitleRow = new JPanel();
        tabelTitleRow.setLayout(new BoxLayout(tabelTitleRow, BoxLayout.Y_AXIS));
        tabelTitleRow.setOpaque(false);

        JLabel lblTabelTitle = new JLabel("Peringatan Stok Menipis");
        lblTabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTabelTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        tabelTitleRow.add(lblTabelTitle);

        JLabel lblTabelSub = new JLabel("Barang dengan stok kurang dari 10");
        lblTabelSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTabelSub.setForeground(ThemeManager.TEXT_MUTED);
        tabelTitleRow.add(lblTabelSub);

        tabelHeader.add(tabelTitleRow, BorderLayout.WEST);

        tabelStokPanel = new JPanel(new BorderLayout());
        tabelStokPanel.setBackground(ThemeManager.BG_SECONDARY);
        tabelStokPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));

        tabelSection.add(tabelHeader,    BorderLayout.NORTH);
        tabelSection.add(tabelStokPanel, BorderLayout.CENTER);

        // Susun semua
        main.add(header);
        main.add(row1);
        main.add(Box.createVerticalStrut(16));
        main.add(row2);
        main.add(tabelSection);

        add(new JScrollPane(main) {{
            setBorder(null);
            getViewport().setBackground(ThemeManager.BG_PRIMARY);
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }}, BorderLayout.CENTER);
    }

    private JPanel makeStatCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_SECONDARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Garis aksen kiri
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(ThemeManager.TEXT_PRIMARY);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setName("VALUE");

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(80, 80, 90));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);

        return card;
    }

    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "VALUE".equals(c.getName())) {
                return (JLabel) c;
            }
        }
        return new JLabel();
    }

    public void loadData() {
        try (Connection con = Koneksi.getKoneksi()) {
            

            // Total transaksi hari ini
            try (PreparedStatement ps = con.prepareStatement(
               "SELECT COUNT(*) FROM tb_penjualan WHERE tgl_transaksi = CURDATE() AND status='selesai'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) lblTotalTransaksi.setText(String.valueOf(rs.getInt(1)));
            }

            // Pendapatan hari ini
            try (PreparedStatement ps = con.prepareStatement(
               "SELECT IFNULL(SUM(total_bayar), 0) FROM tb_penjualan WHERE tgl_transaksi = CURDATE() AND status='selesai'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) lblPendapatanHariIni.setText(FMT.format(rs.getDouble(1)));
            }

            // Total customer
            try (Statement st = con.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tb_customer");
                if (rs.next()) lblTotalCustomer.setText(String.valueOf(rs.getInt(1)));
            }

            // Total barang
            try (Statement st = con.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tb_barang");
                if (rs.next()) lblTotalBarang.setText(String.valueOf(rs.getInt(1)));
            }

            // Stok menipis (< 10)
            try (Statement st = con.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tb_barang WHERE stok < 10");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    lblStokMenipis.setText(String.valueOf(count));
                    if (count > 0) lblStokMenipis.setForeground(new Color(239, 68, 68));
                    else lblStokMenipis.setForeground(new Color(34, 197, 94));
                }
            }

            // Tabel stok menipis
            loadTabelStokMenipis(con);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTabelStokMenipis(Connection con) throws Exception {
        javax.swing.table.DefaultTableModel stokModel = new javax.swing.table.DefaultTableModel(
            new String[]{"ID Barang", "Nama Barang", "Kategori", "Stok", "Satuan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        String sql = "SELECT b.id_barang, b.nama_barang, k.nama_kategori, b.stok, b.satuan "
                   + "FROM tb_barang b JOIN tb_kategori k ON b.id_kategori = k.id_kategori "
                   + "WHERE b.stok < 10 ORDER BY b.stok ASC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stokModel.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getInt("stok"),
                    rs.getString("satuan")
                });
            }
        }

        JTable tabelStok = new JTable(stokModel);
        ThemeManager.styleTable(tabelStok);

        // Warnai baris berdasarkan stok
        tabelStok.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                    int stok = (int) stokModel.getValueAt(row, 3);
                    if (!isSel) {
                        if (stok == 0)      setBackground(new Color(60, 20, 20));
                        else if (stok <= 5) setBackground(new Color(50, 25, 15));
                        else                setBackground(row % 2 == 0 ?
                            ThemeManager.TABLE_ROW_EVEN : ThemeManager.TABLE_ROW_ODD);
                    }
                    setForeground(stok == 0 ? new Color(239, 68, 68) : Color.WHITE);
                    setBorder(new EmptyBorder(0, 12, 0, 12));
                    return this;
                }
            });

        JScrollPane scrollStok = new JScrollPane(tabelStok);
        ThemeManager.styleScrollPane(scrollStok);
        scrollStok.setPreferredSize(new Dimension(0, 200));

        tabelStokPanel.removeAll();
        if (stokModel.getRowCount() == 0) {
            JLabel lblOk = new JLabel("✅  Semua stok barang aman");
            lblOk.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            lblOk.setForeground(new Color(34, 197, 94));
            lblOk.setHorizontalAlignment(SwingConstants.CENTER);
            tabelStokPanel.add(lblOk, BorderLayout.CENTER);
        } else {
            tabelStokPanel.add(scrollStok, BorderLayout.CENTER);
        }
        tabelStokPanel.revalidate();
        tabelStokPanel.repaint();
    }

    @Override
    public void refreshData() {
        loadData();
    }
} 
