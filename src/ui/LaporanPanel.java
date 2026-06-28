package ui;

import koneksi.Koneksi;
import util.Session;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;

public class LaporanPanel extends JPanel {

    private JTextField        txtDari, txtSampai, txtCari;
    private JButton btnTampilkan, btnHariIni, btnSemuaData, btnCetakLaporan;
    private JTable            tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel            lblTotal, lblJumlahTransaksi;
    private String cetakMode = "";
    private String cetakDari  = null;
    private String cetakSampai = null;

    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(
        new java.util.Locale("id", "ID"));

    public LaporanPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        // Petugas hanya lihat hari ini, Admin lihat semua
        if ("Petugas".equals(Session.level)) {
            loadHariIni();
        } else {
            loadSemuaData();
        }
    }

    private void buildUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeManager.BG_SECONDARY);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER),
            new EmptyBorder(32, 28, 32, 28)
        ));
        sidebar.setPreferredSize(new Dimension(290, 0));

        sidebar.add(makeBadge("LAPORAN PENJUALAN"));
        sidebar.add(Box.createVerticalStrut(16));

        JLabel lblTitle = new JLabel("Laporan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Riwayat transaksi penjualan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(makeLabel("DARI TANGGAL"));
        sidebar.add(Box.createVerticalStrut(6));
        txtDari = new JTextField();
        txtDari.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtDari.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDari.setToolTipText("Format: YYYY-MM-DD");
        styleField(txtDari);
        sidebar.add(txtDari);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeLabel("SAMPAI TANGGAL"));
        sidebar.add(Box.createVerticalStrut(6));
        txtSampai = new JTextField();
        txtSampai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtSampai.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSampai.setToolTipText("Format: YYYY-MM-DD");
        styleField(txtSampai);
        sidebar.add(txtSampai);
        sidebar.add(Box.createVerticalStrut(24));

        btnTampilkan = makeBtn("Tampilkan",  ThemeManager.ACCENT);
        btnHariIni   = makeBtn("Hari Ini",   new Color(34, 197, 94));
        btnSemuaData = makeBtn("Semua Data", ThemeManager.ACCENT_BLUE);

        sidebar.add(btnTampilkan); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnHariIni);   sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnSemuaData);
        sidebar.add(Box.createVerticalStrut(8));
        btnCetakLaporan = makeBtn("Cetak Laporan", new Color(59, 130, 246));
        btnCetakLaporan.setEnabled(false);
        sidebar.add(btnCetakLaporan);

        // ── Pembatasan Petugas ────────────────────────────
        if ("Petugas".equals(Session.level)) {
            txtDari.setEditable(false);
            txtSampai.setEditable(false);
            btnSemuaData.setVisible(false);
            btnTampilkan.setVisible(false);

            JLabel lblInfo = new JLabel("⚠ Hanya data hari ini");
            lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblInfo.setForeground(new Color(234, 179, 8));
            lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(lblInfo);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(24));

        // Card total
        JPanel cardTotal = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(249, 115, 22, 30),
                    getWidth(), getHeight(), new Color(239, 68, 68, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(249, 115, 22, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardTotal.setLayout(new BoxLayout(cardTotal, BoxLayout.Y_AXIS));
        cardTotal.setOpaque(false);
        cardTotal.setBorder(new EmptyBorder(16, 16, 16, 16));
        cardTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cardTotal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotalLabel = new JLabel("TOTAL PENDAPATAN");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTotalLabel.setForeground(new Color(180, 100, 50));
        lblTotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTotal.add(lblTotalLabel);
        cardTotal.add(Box.createVerticalStrut(8));

        lblTotal = new JLabel("Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(ThemeManager.ACCENT);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTotal.add(lblTotal);
        cardTotal.add(Box.createVerticalStrut(6));

        lblJumlahTransaksi = new JLabel("0 item");
        lblJumlahTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblJumlahTransaksi.setForeground(new Color(140, 80, 40));
        lblJumlahTransaksi.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTotal.add(lblJumlahTransaksi);
        sidebar.add(cardTotal);

        // ── Area Tabel (kanan) ────────────────────────────
        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setBackground(ThemeManager.BG_PRIMARY);
        tableArea.setBorder(new EmptyBorder(32, 28, 32, 32));

        JPanel tableHeader = new JPanel(new BorderLayout(12, 0));
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.Y_AXIS));
        titleRow.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Riwayat Penjualan");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        titleRow.add(lblTableTitle);

        JLabel lblTableSub = new JLabel(
            "Petugas".equals(Session.level)
                ? "Transaksi hari ini"
                : "Detail transaksi per item barang"
        );
        lblTableSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSub.setForeground(ThemeManager.TEXT_MUTED);
        titleRow.add(lblTableSub);

        // Search box
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(ThemeManager.BG_SECONDARY);
        searchWrapper.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        searchWrapper.setPreferredSize(new Dimension(220, 40));

        JLabel lblSearch = new JLabel("🔍");
        lblSearch.setForeground(ThemeManager.TEXT_SECONDARY);
        lblSearch.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtCari = new JTextField();
        txtCari.setBackground(ThemeManager.BG_SECONDARY);
        txtCari.setForeground(ThemeManager.TEXT_PRIMARY);
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCari.setCaretColor(ThemeManager.ACCENT);
        txtCari.setBorder(new EmptyBorder(6, 4, 6, 8));

        searchWrapper.add(lblSearch, BorderLayout.WEST);
        searchWrapper.add(txtCari,   BorderLayout.CENTER);

        tableHeader.add(titleRow,      BorderLayout.WEST);
        tableHeader.add(searchWrapper, BorderLayout.EAST);

        model = new DefaultTableModel(
            new String[]{"No Faktur","Tanggal","Customer","Barang","Jml","Subtotal","Kasir"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 1) return java.sql.Date.class;
                if (c == 4) return Integer.class;
                return String.class;
            }
        };
        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabel);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(120);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabel.getColumnModel().getColumn(5).setPreferredWidth(120);

        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabel);
        ThemeManager.styleScrollPane(scroll);

        tableArea.add(tableHeader, BorderLayout.NORTH);
        tableArea.add(scroll,      BorderLayout.CENTER);

        add(sidebar,   BorderLayout.WEST);
        add(tableArea, BorderLayout.CENTER);

        btnTampilkan.addActionListener(e -> {
        cetakMode = "filter";
        cetakDari = txtDari.getText();
        cetakSampai = txtSampai.getText();
        loadByRange();
        btnCetakLaporan.setEnabled(true);
     });
        btnHariIni.addActionListener(e -> {
        cetakMode = "hariini";
        loadHariIni();
        btnCetakLaporan.setEnabled(true);
     });
        btnSemuaData.addActionListener(e -> {
        cetakMode = "semua";
        loadSemuaData();
        btnCetakLaporan.setEnabled(true);
     });
        btnCetakLaporan.addActionListener(e -> cetakLaporan());

        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
    }

    private void filter() {
        String keyword = txtCari.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null :
            RowFilter.regexFilter("(?i)" + keyword));
    }

    private JLabel makeBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(249, 115, 22, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(249, 115, 22, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(ThemeManager.ACCENT);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        return badge;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.BORDER);
        sep.setBackground(ThemeManager.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField f) {
        f.setBackground(ThemeManager.BG_SECONDARY);
        f.setForeground(ThemeManager.TEXT_PRIMARY);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setCaretColor(ThemeManager.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_HOVER),
            new EmptyBorder(10, 14, 10, 14)
        ));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (bg.equals(ThemeManager.ACCENT)) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(249, 115, 22),
                        getWidth(), 0, new Color(239, 68, 68));
                    g2.setPaint(gp);
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color darker = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(darker);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public void loadSemuaData() {
        txtDari.setText(""); txtSampai.setText("");
        loadData(buildQuery(false), null, null);
    }

    public void loadHariIni() {
        String hari = java.time.LocalDate.now().toString();
        txtDari.setText(hari); txtSampai.setText(hari);
        loadData(buildQuery(true), hari, hari);
    }

    private void loadByRange() {
        String dari   = txtDari.getText().trim();
        String sampai = txtSampai.getText().trim();
        if (dari.isEmpty() || sampai.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Isi kedua kolom tanggal!\nFormat: YYYY-MM-DD",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadData(buildQuery(true), dari, sampai);
    }

    private String buildQuery(boolean withFilter) {
        String sql = "SELECT p.no_faktur, p.tgl_transaksi, c.nama_customer, "
                   + "b.nama_barang, d.jumlah_beli, d.subtotal, u.nama_lengkap "
                   + "FROM tb_penjualan p "
                   + "JOIN tb_customer c         ON p.id_customer = c.id_customer "
                   + "JOIN tb_detail_penjualan d ON p.id_jual     = d.id_jual "
                   + "JOIN tb_barang b            ON d.id_barang  = b.id_barang "
                   + "JOIN tb_user u              ON p.id_user    = u.id_user ";
        
        if (withFilter)
        sql += "WHERE p.tgl_transaksi BETWEEN ? AND ? AND p.status='selesai' ";
    else
        sql += "WHERE p.status='selesai' ";
    sql += "ORDER BY p.id_jual DESC";
    return sql;
}

    private void loadData(String sql, String dari, String sampai) {
        model.setRowCount(0);
        txtCari.setText("");
        double grandTotal = 0;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (dari != null) {
                ps.setDate(1, java.sql.Date.valueOf(dari));
                ps.setDate(2, java.sql.Date.valueOf(sampai));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double subtotal = rs.getDouble("subtotal");
                grandTotal += subtotal;
                model.addRow(new Object[]{
                    rs.getString("no_faktur"),
                    rs.getDate("tgl_transaksi"),
                    rs.getString("nama_customer"),
                    rs.getString("nama_barang"),
                    rs.getInt("jumlah_beli"),
                    FMT.format(subtotal),
                    rs.getString("nama_lengkap")
                });
            }

            lblTotal.setText(FMT.format(grandTotal));
            lblJumlahTransaksi.setText(model.getRowCount() + " item");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Format tanggal salah!\nGunakan: YYYY-MM-DD",
                "Format Tanggal", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void cetakLaporan() {
    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak!");
        return;
    }
    try {
        String fileName    = "Laporan-" + java.time.LocalDate.now() + ".pdf";
        String desktopPath = System.getProperty("user.home") + "/Desktop/" + fileName;

        Document doc = new Document(new com.lowagie.text.Rectangle(
            PageSize.A4.getHeight(), PageSize.A4.getWidth()), 30, 30, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(desktopPath));
        doc.open();

        com.lowagie.text.Font fTitle  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fBold   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.NORMAL);
        com.lowagie.text.Font fSmall  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  8, com.lowagie.text.Font.NORMAL);

        Paragraph title = new Paragraph("TOKO BERKAH JAYA", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Laporan Penjualan", fBold);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);

        String periodeText;
        if ("hariini".equals(cetakMode))
            periodeText = "Periode: " + java.time.LocalDate.now();
        else if ("semua".equals(cetakMode))
            periodeText = "Periode: Semua Data";
        else
            periodeText = "Periode: " + cetakDari + " s/d " + cetakSampai;

        Paragraph periode = new Paragraph(periodeText, fSmall);
        periode.setAlignment(Element.ALIGN_CENTER);
        doc.add(periode);
        doc.add(new Paragraph(" ", fSmall));

        PdfPTable tbl = new PdfPTable(7);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{2.5f, 1.8f, 2.5f, 2.5f, 0.8f, 2f, 2f});
        tbl.setSpacingBefore(8);

        for (String h : new String[]{"No Faktur","Tanggal","Customer","Barang","Qty","Subtotal","Kasir"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, fBold));
            c.setBackgroundColor(new java.awt.Color(200, 200, 200));
            c.setPadding(4);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tbl.addCell(c);
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < 7; j++) {
                Object val = model.getValueAt(i, j);
                PdfPCell c = new PdfPCell(new Phrase(val != null ? val.toString() : "", fNormal));
                c.setPadding(3);
                if (j == 4 || j == 5) c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tbl.addCell(c);
            }
        }
        doc.add(tbl);

        doc.add(new Paragraph(" ", fSmall));
        Paragraph total = new Paragraph("Total Pendapatan: " + lblTotal.getText(), fBold);
        total.setAlignment(Element.ALIGN_RIGHT);
        doc.add(total);

        Paragraph jumlah = new Paragraph("Jumlah Item: " + model.getRowCount(), fSmall);
        jumlah.setAlignment(Element.ALIGN_RIGHT);
        doc.add(jumlah);

        doc.close();
        Desktop.getDesktop().open(new File(desktopPath));
        JOptionPane.showMessageDialog(this, "Laporan disimpan di Desktop: " + fileName);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal cetak laporan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}