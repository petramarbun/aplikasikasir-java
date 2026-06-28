package ui;

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

import koneksi.Koneksi;
import util.Session;
import util.ThemeManager;
import util.DatePicker;
import util.Refreshable;
import util.InputValidator;
import util.ComboBoxSearch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.table.TableRowSorter;


public class TransaksiPanel extends JPanel implements Refreshable {

    private JComboBox<String> cbCustomer, cbBarang;
    private DatePicker        datePicker;
    private JTextField        txtHarga, txtNoFaktur;
    private JSpinner          spnJumlah;
    private JLabel            lblStokInfo, lblGrandTotal;
    private JButton           btnTambahItem, btnHapusItem, btnSimpan, btnBersih, btnRefresh;
    private JTextField txtUangBayar;
    private JLabel lblKembalian;
    private double grandTotalValue = 0;

    private JTable            tabelKeranjang;
    private DefaultTableModel modelKeranjang;

    private JTable            tabelRiwayat;
    private DefaultTableModel modelRiwayat;
    private JTextField txtCariRiwayat;
    private TableRowSorter<DefaultTableModel> sorterRiwayat;
    
    private JTextField txtCariKeranjang;
    private TableRowSorter<DefaultTableModel> sorterKeranjang;

    private java.util.List<String> idCustomerList = new ArrayList<>();
    private java.util.List<String> idBarangList   = new ArrayList<>();
    private java.util.List<Integer> idJualList = new ArrayList<>();
    private java.util.Map<String, Double>  hargaMap = new HashMap<>();
    private java.util.Map<String, Integer> stokMap  = new HashMap<>();

    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(
        new java.util.Locale("id", "ID"));

    public TransaksiPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        loadCustomer();
        loadBarang();
        generateNoFaktur();
        loadRiwayatHariIni();
        InputValidator.angkaOnly(txtUangBayar, 12);
    }

    private void buildUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeManager.BG_SECONDARY);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER),
            new EmptyBorder(20, 22, 20, 22)
        ));
        sidebar.setPreferredSize(new Dimension(300, 0));

        sidebar.add(makeBadge("TRANSAKSI PENJUALAN"));
        sidebar.add(Box.createVerticalStrut(10));

        JLabel lblTitle = new JLabel("Form Transaksi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Tambah item lalu simpan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(12));

        // No Faktur
        sidebar.add(makeLabel("NO. FAKTUR"));
        sidebar.add(Box.createVerticalStrut(4));
        txtNoFaktur = new JTextField();
        txtNoFaktur.setEditable(false);
        txtNoFaktur.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtNoFaktur.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNoFaktur.setBackground(ThemeManager.BG_SECONDARY);
        txtNoFaktur.setForeground(ThemeManager.ACCENT);
        txtNoFaktur.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtNoFaktur.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(249, 115, 22, 40)),
            new EmptyBorder(6, 12, 6, 12)
        ));
        sidebar.add(txtNoFaktur);
        sidebar.add(Box.createVerticalStrut(8));

        // Tanggal - DatePicker
        sidebar.add(makeLabel("TANGGAL"));
        sidebar.add(Box.createVerticalStrut(4));
        datePicker = new DatePicker();
        sidebar.add(datePicker);
        sidebar.add(Box.createVerticalStrut(8));

        // Customer
        sidebar.add(makeLabel("CUSTOMER"));
        sidebar.add(Box.createVerticalStrut(4));
        cbCustomer = new JComboBox<>();
        styleCombo(cbCustomer);
        ThemeManager.styleComboBox(cbCustomer);
        cbCustomer.setForeground(ThemeManager.TEXT_PRIMARY);
        ComboBoxSearch.enable(cbCustomer);
        sidebar.add(cbCustomer);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(10));

        // Barang
        sidebar.add(makeLabel("TAMBAH BARANG"));
        sidebar.add(Box.createVerticalStrut(4));
        cbBarang = new JComboBox<>();
        styleCombo(cbBarang);
        ThemeManager.styleComboBox(cbBarang);
        cbBarang.setForeground(ThemeManager.TEXT_PRIMARY);
        ComboBoxSearch.enable(cbBarang);
        sidebar.add(cbBarang);
        sidebar.add(Box.createVerticalStrut(4));

        lblStokInfo = new JLabel("Stok tersedia: -");
        lblStokInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStokInfo.setForeground(ThemeManager.ACCENT_GREEN);
        lblStokInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblStokInfo);
        sidebar.add(Box.createVerticalStrut(8));

        // Harga + Jumlah
        JPanel rowHJ = new JPanel(new GridLayout(1, 2, 8, 0));
        rowHJ.setOpaque(false);
        rowHJ.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        rowHJ.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel colH = new JPanel();
        colH.setLayout(new BoxLayout(colH, BoxLayout.Y_AXIS));
        colH.setOpaque(false);
        colH.add(makeLabel("HARGA"));
        colH.add(Box.createVerticalStrut(4));
        txtHarga = new JTextField();
        txtHarga.setEditable(false);
        txtHarga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtHarga.setBackground(ThemeManager.BG_SECONDARY);
        txtHarga.setForeground(Color.BLACK);
        txtHarga.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtHarga.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_SECONDARY),
            new EmptyBorder(6, 10, 6, 10)
        ));
        colH.add(txtHarga);

        JPanel colJ = new JPanel();
        colJ.setLayout(new BoxLayout(colJ, BoxLayout.Y_AXIS));
        colJ.setOpaque(false);
        colJ.add(makeLabel("JUMLAH"));
        colJ.add(Box.createVerticalStrut(4));
        spnJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnJumlah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JComponent editor = spnJumlah.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(ThemeManager.BG_SECONDARY);
            tf.setForeground(ThemeManager.TEXT_PRIMARY);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tf.setCaretColor(ThemeManager.ACCENT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BG_HOVER),
                new EmptyBorder(6, 8, 6, 8)
            ));
        }
        colJ.add(spnJumlah);

        rowHJ.add(colH);
        rowHJ.add(colJ);
        sidebar.add(rowHJ);
        sidebar.add(Box.createVerticalStrut(10));

        btnTambahItem = makeBtn("+ Tambah ke Keranjang", ThemeManager.ACCENT);
        sidebar.add(btnTambahItem);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(10));


        // Ringkasan Pembayaran
            JPanel ringkasanBox = new JPanel();
            ringkasanBox.setLayout(new BoxLayout(ringkasanBox, BoxLayout.Y_AXIS));
            ringkasanBox.setBackground(ThemeManager.BG_SECONDARY);
            ringkasanBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER),
                new EmptyBorder(10, 12, 10, 12)
            ));
            ringkasanBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            ringkasanBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JPanel rowTotal = new JPanel(new BorderLayout());
            rowTotal.setOpaque(false);
            rowTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblTotalCaption = new JLabel("Total");
            lblTotalCaption.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTotalCaption.setForeground(new Color(140, 140, 150));
            lblGrandTotal = new JLabel("Rp 0");
            lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblGrandTotal.setForeground(ThemeManager.ACCENT);
            rowTotal.add(lblTotalCaption, BorderLayout.WEST);
            rowTotal.add(lblGrandTotal, BorderLayout.EAST);
            ringkasanBox.add(rowTotal);
            ringkasanBox.add(Box.createVerticalStrut(6));

            JPanel rowBayar = new JPanel(new BorderLayout(8, 0));
            rowBayar.setOpaque(false);
            rowBayar.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblBayarCaption = new JLabel("Bayar");
            lblBayarCaption.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblBayarCaption.setForeground(new Color(140, 140, 150));

            txtUangBayar = new JTextField();
            txtUangBayar.setBackground(ThemeManager.BG_SECONDARY);
            txtUangBayar.setForeground(ThemeManager.TEXT_PRIMARY);
            txtUangBayar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtUangBayar.setCaretColor(ThemeManager.ACCENT);
            txtUangBayar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER),
                new EmptyBorder(4, 8, 4, 8)
            ));
            txtUangBayar.setPreferredSize(new Dimension(100, 28));

            rowBayar.add(lblBayarCaption, BorderLayout.WEST);
            rowBayar.add(txtUangBayar, BorderLayout.CENTER);
            ringkasanBox.add(rowBayar);
            ringkasanBox.add(Box.createVerticalStrut(6));

            lblKembalian = new JLabel("Kembalian: Rp 0");
            lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblKembalian.setForeground(new Color(34, 197, 94));
            lblKembalian.setAlignmentX(Component.LEFT_ALIGNMENT);
            ringkasanBox.add(lblKembalian);

            sidebar.add(ringkasanBox);
            sidebar.add(Box.createVerticalStrut(10));


        txtUangBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
        });
        
        btnSimpan    = makeBtn("Simpan Transaksi",    ThemeManager.ACCENT);
        btnHapusItem = makeBtn("Hapus Item Terpilih", new Color(239, 68, 68));
        btnBersih    = makeBtn("Bersih / Reset",      ThemeManager.ACCENT_BLUE);
        btnRefresh   = makeBtn("Refresh Data",        ThemeManager.ACCENT_BLUE);

        sidebar.add(btnSimpan);    sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnHapusItem); sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnBersih);    sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnRefresh);

        // Area Kanan
        JPanel rightArea = new JPanel(new BorderLayout(0, 16));
        rightArea.setBackground(ThemeManager.BG_PRIMARY);
        rightArea.setBorder(new EmptyBorder(24, 22, 24, 24));

        // Keranjang
        JPanel keranjangPanel = new JPanel(new BorderLayout());
        keranjangPanel.setBackground(ThemeManager.BG_PRIMARY);

        JPanel keranjangHeader = new JPanel(new BorderLayout());
        keranjangHeader.setOpaque(false);
        keranjangHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblKeranjang = new JLabel("Keranjang Belanja");
        lblKeranjang.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblKeranjang.setForeground(ThemeManager.TEXT_PRIMARY);
        keranjangHeader.add(lblKeranjang, BorderLayout.WEST);
        
        // Search box keranjang
        JPanel searchWrapperKeranjang = new JPanel(new BorderLayout());
        searchWrapperKeranjang.setBackground(ThemeManager.BG_SECONDARY);
        searchWrapperKeranjang.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        searchWrapperKeranjang.setPreferredSize(new Dimension(200, 36));

        JLabel lblSearchKeranjang = new JLabel("🔍");
        lblSearchKeranjang.setForeground(ThemeManager.TEXT_SECONDARY);
        lblSearchKeranjang.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtCariKeranjang = new JTextField();
        txtCariKeranjang.setBackground(ThemeManager.BG_SECONDARY);
        txtCariKeranjang.setForeground(ThemeManager.TEXT_PRIMARY);
        txtCariKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCariKeranjang.setCaretColor(ThemeManager.ACCENT);
        txtCariKeranjang.setBorder(new EmptyBorder(6, 4, 6, 8));

        searchWrapperKeranjang.add(lblSearchKeranjang, BorderLayout.WEST);
        searchWrapperKeranjang.add(txtCariKeranjang, BorderLayout.CENTER);

        keranjangHeader.add(searchWrapperKeranjang, BorderLayout.EAST);

        modelKeranjang = new DefaultTableModel(
            new String[]{"ID Barang","Nama Barang","Harga","Jumlah","Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : String.class;
            }
        };
        tabelKeranjang = new JTable(modelKeranjang);
        tabelKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabelKeranjang);
        
        sorterKeranjang = new TableRowSorter<>(modelKeranjang);
        tabelKeranjang.setRowSorter(sorterKeranjang);

        txtCariKeranjang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterKeranjang(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterKeranjang(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterKeranjang(); }
        });
        
        JScrollPane scrollKeranjang = new JScrollPane(tabelKeranjang);
        ThemeManager.styleScrollPane(scrollKeranjang);
        scrollKeranjang.setPreferredSize(new Dimension(0, 160));

        keranjangPanel.add(keranjangHeader, BorderLayout.NORTH);
        keranjangPanel.add(scrollKeranjang, BorderLayout.CENTER);

        // Riwayat
        JPanel riwayatPanel = new JPanel(new BorderLayout());
        riwayatPanel.setBackground(ThemeManager.BG_PRIMARY);

        JPanel riwayatHeader = new JPanel(new BorderLayout());
        riwayatHeader.setOpaque(false);
        riwayatHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel riwayatTitle = new JPanel();
        riwayatTitle.setLayout(new BoxLayout(riwayatTitle, BoxLayout.Y_AXIS));
        riwayatTitle.setOpaque(false);

        JLabel lblRiwayat = new JLabel("Riwayat Hari Ini");
        lblRiwayat.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRiwayat.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel lblRiwayatSub = new JLabel("Transaksi yang tercatat hari ini");
        lblRiwayatSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRiwayatSub.setForeground(ThemeManager.TEXT_MUTED);

        riwayatTitle.add(lblRiwayat);
        riwayatTitle.add(lblRiwayatSub);
        riwayatHeader.add(riwayatTitle, BorderLayout.WEST);
        
        // Search box riwayat
        JPanel searchWrapperRiwayat = new JPanel(new BorderLayout());
        searchWrapperRiwayat.setBackground(ThemeManager.BG_SECONDARY);
        searchWrapperRiwayat.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        searchWrapperRiwayat.setPreferredSize(new Dimension(200, 36));

        JLabel lblSearchRiwayat = new JLabel("🔍");
        lblSearchRiwayat.setForeground(ThemeManager.TEXT_SECONDARY);
        lblSearchRiwayat.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtCariRiwayat = new JTextField();
        txtCariRiwayat.setBackground(ThemeManager.BG_SECONDARY);
        txtCariRiwayat.setForeground(ThemeManager.TEXT_PRIMARY);
        txtCariRiwayat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCariRiwayat.setCaretColor(ThemeManager.ACCENT);
        txtCariRiwayat.setBorder(new EmptyBorder(6, 4, 6, 8));

        searchWrapperRiwayat.add(lblSearchRiwayat, BorderLayout.WEST);
        searchWrapperRiwayat.add(txtCariRiwayat, BorderLayout.CENTER);

        riwayatHeader.add(searchWrapperRiwayat, BorderLayout.EAST);

        modelRiwayat = new DefaultTableModel(
            new String[]{"No Faktur","Tanggal","Customer","Total","Kasir","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 1 ? java.sql.Date.class : String.class;
            }
        };
        tabelRiwayat = new JTable(modelRiwayat);
        tabelRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabelRiwayat);
        
        sorterRiwayat = new TableRowSorter<>(modelRiwayat);
        tabelRiwayat.setRowSorter(sorterRiwayat);

        txtCariRiwayat.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterRiwayat(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterRiwayat(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterRiwayat(); }
});

        JScrollPane scrollRiwayat = new JScrollPane(tabelRiwayat);
        ThemeManager.styleScrollPane(scrollRiwayat);

        JButton btnBatalkan = new JButton("Batalkan Transaksi");
        btnBatalkan.setBackground(new Color(217, 119, 6));
        btnBatalkan.setForeground(ThemeManager.TEXT_PRIMARY);
        btnBatalkan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBatalkan.setFocusPainted(false);
        btnBatalkan.setBorderPainted(false);
        btnBatalkan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBatalkan.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnBatalkan.addActionListener(e -> batalkanTransaksi());

        JButton btnCetakResi = new JButton("Cetak Resi");
        btnCetakResi.setBackground(new Color(34, 197, 94));
        btnCetakResi.setForeground(ThemeManager.TEXT_PRIMARY);
        btnCetakResi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCetakResi.setFocusPainted(false);
        btnCetakResi.setBorderPainted(false);
        btnCetakResi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCetakResi.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnCetakResi.addActionListener(e -> cetakResi());

        JPanel riwayatActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        riwayatActionRow.setOpaque(false);
        riwayatActionRow.add(btnCetakResi);
        riwayatActionRow.add(btnBatalkan);

        JPanel riwayatTop = new JPanel(new BorderLayout());
        riwayatTop.setOpaque(false);
        riwayatTop.add(riwayatHeader, BorderLayout.NORTH);
        riwayatTop.add(riwayatActionRow, BorderLayout.SOUTH);

        riwayatPanel.add(riwayatTop, BorderLayout.NORTH);
        riwayatPanel.add(scrollRiwayat, BorderLayout.CENTER);

        rightArea.add(keranjangPanel, BorderLayout.NORTH);
        rightArea.add(riwayatPanel,   BorderLayout.CENTER);

        add(sidebar,   BorderLayout.WEST);
        add(rightArea, BorderLayout.CENTER);

        cbBarang.addActionListener(e -> onBarangSelected());
        btnTambahItem.addActionListener(e -> tambahKeKeranjang());
        btnHapusItem.addActionListener(e  -> hapusItemKeranjang());
        btnSimpan.addActionListener(e     -> simpanTransaksi());
        btnBersih.addActionListener(e     -> bersihForm());
        btnRefresh.addActionListener(e    -> {
            loadCustomer(); loadBarang(); loadRiwayatHariIni();
            JOptionPane.showMessageDialog(this, "Data berhasil di-refresh.");
        });
    }

    private void filterRiwayat() {
        String keyword = txtCariRiwayat.getText().trim();
        sorterRiwayat.setRowFilter(keyword.isEmpty() ? null :
            RowFilter.regexFilter("(?i)" + keyword));
    }
    
    private void filterKeranjang() {
    String keyword = txtCariKeranjang.getText().trim();
    sorterKeranjang.setRowFilter(keyword.isEmpty() ? null :
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

    private void styleCombo(JComboBox<String> cb) {
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setBackground(ThemeManager.BG_SECONDARY);
        cb.setForeground(Color.BLACK);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        if (cb.getEditor() != null) {
            cb.getEditor().getEditorComponent().setForeground(ThemeManager.TEXT_PRIMARY);
            cb.getEditor().getEditorComponent().setBackground(ThemeManager.BG_SECONDARY);
        }

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
                setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.BG_SECONDARY);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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

    public void loadCustomer() {
        cbCustomer.removeAllItems();
        idCustomerList.clear();
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id_customer, nama_customer FROM tb_customer ORDER BY id_customer")) {
            while (rs.next()) {
                idCustomerList.add(rs.getString("id_customer"));
                cbCustomer.addItem(rs.getString("id_customer") + " – " +
                    rs.getString("nama_customer"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        ComboBoxSearch.refresh(cbCustomer);
    }

    public void loadBarang() {
        cbBarang.removeAllItems();
        idBarangList.clear();
        hargaMap.clear();
        stokMap.clear();
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id_barang, nama_barang, harga_jual, stok "
                 + "FROM tb_barang ORDER BY id_barang")) {
            while (rs.next()) {
                String id = rs.getString("id_barang");
                idBarangList.add(id);
                hargaMap.put(id, rs.getDouble("harga_jual"));
                stokMap.put(id, rs.getInt("stok"));
                cbBarang.addItem(id + " – " + rs.getString("nama_barang")
                    + " (Stok: " + rs.getInt("stok") + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        ComboBoxSearch.refresh(cbBarang);
        onBarangSelected();
    }

    private void generateNoFaktur() {
        String tanggal = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noFaktur = "FAK-" + tanggal + "-" +
            String.format("%03d", (int)(Math.random() * 900) + 100);
        txtNoFaktur.setText(noFaktur);
    }

    private void onBarangSelected() {
        Object sel = cbBarang.getSelectedItem();
        if (sel == null || idBarangList.isEmpty()) {
            txtHarga.setText("");
            lblStokInfo.setText("Stok tersedia: -");
            return;
        }
        String idBarang = sel.toString().split(" – ")[0].trim();
        if (!hargaMap.containsKey(idBarang)) {
            txtHarga.setText("");
            lblStokInfo.setText("Stok tersedia: -");
            return;
        }
        double harga = hargaMap.getOrDefault(idBarang, 0.0);
        int    stok  = stokMap.getOrDefault(idBarang, 0);

        txtHarga.setText(String.valueOf(harga));

        if (stok <= 0) {
            lblStokInfo.setText("Stok HABIS!");
            lblStokInfo.setForeground(ThemeManager.ACCENT_RED);
        } else {
            lblStokInfo.setText("Stok tersedia: " + stok);
            lblStokInfo.setForeground(ThemeManager.ACCENT_GREEN);
        }
    }

    private void tambahKeKeranjang() {
        if (cbBarang.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu!",
                "Validasi", JOptionPane.WARNING_MESSAGE); return;
        }

        String idBarang   = cbBarang.getSelectedItem().toString().split(" – ")[0].trim();
        String namaBarang = cbBarang.getSelectedItem().toString()
            .replaceAll(" \\(Stok:.*\\)", "")
            .replaceAll("^[^ ]+ – ", "");
        double harga  = hargaMap.getOrDefault(idBarang, 0.0);
        int    jumlah = (int) spnJumlah.getValue();
        int    stokDB = getStokFromDB(idBarang);

        if (stokDB <= 0) {
            JOptionPane.showMessageDialog(this, "Stok barang HABIS!",
                "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE); return;
        }

        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            if (modelKeranjang.getValueAt(i, 0).toString().equals(idBarang)) {
                int jumlahBaru = (int) modelKeranjang.getValueAt(i, 3) + jumlah;
                if (jumlahBaru > stokDB) {
                    JOptionPane.showMessageDialog(this,
                        "Total jumlah melebihi stok!\nStok tersedia: " + stokDB,
                        "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE); return;
                }
                modelKeranjang.setValueAt(jumlahBaru, i, 3);
                modelKeranjang.setValueAt(FMT.format(harga * jumlahBaru), i, 4);
                hitungGrandTotal();
                return;
            }
        }

        if (jumlah > stokDB) {
            JOptionPane.showMessageDialog(this,
                "Stok tidak mencukupi!\nStok tersedia: " + stokDB,
                "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE); return;
        }

        modelKeranjang.addRow(new Object[]{
            idBarang, namaBarang, FMT.format(harga), jumlah, FMT.format(harga * jumlah)
        });
        hitungGrandTotal();
    }

    private void hapusItemKeranjang() {
        int row = tabelKeranjang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih item dari keranjang!"); return;
        }
        modelKeranjang.removeRow(row);
        hitungGrandTotal();
    }

    private void hitungGrandTotal() {
        double total = 0;
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            double h = hargaMap.getOrDefault(
                modelKeranjang.getValueAt(i, 0).toString(), 0.0);
            int j = (int) modelKeranjang.getValueAt(i, 3);
            total += h * j;
        }
        grandTotalValue = total;
        lblGrandTotal.setText(FMT.format(total));
        hitungKembalian();
    }

    private void simpanTransaksi() {
        if (cbCustomer.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Pilih customer terlebih dahulu!",
                "Validasi", JOptionPane.WARNING_MESSAGE); return;
        }
        if (modelKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!",
                "Validasi", JOptionPane.WARNING_MESSAGE); return;
        }

        String idCustomer = cbCustomer.getSelectedItem().toString().split(" – ")[0].trim();
        String noFaktur   = txtNoFaktur.getText();
        LocalDate tglTransaksi = datePicker.getDate();

        double grandTotal = 0;
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            double h = hargaMap.getOrDefault(
                modelKeranjang.getValueAt(i, 0).toString(), 0.0);
            int j = (int) modelKeranjang.getValueAt(i, 3);
            grandTotal += h * j;
        }

        int ok = JOptionPane.showConfirmDialog(this,
            "Konfirmasi Transaksi:\n"
            + "No Faktur : " + noFaktur + "\n"
            + "Tanggal   : " + tglTransaksi + "\n"
            + "Customer  : " + cbCustomer.getSelectedItem() + "\n"
            + "Item      : " + modelKeranjang.getRowCount() + " barang\n"
            + "Total     : " + FMT.format(grandTotal) + "\n\nSimpan transaksi?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        Connection con = null;
        try {
            con = Koneksi.getKoneksi();
            con.setAutoCommit(false);

            int idJual;
            try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO tb_penjualan (no_faktur,tgl_transaksi,id_customer,total_bayar,id_user) "
                + "VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, noFaktur);
                ps.setDate(2, java.sql.Date.valueOf(tglTransaksi));
                ps.setString(3, idCustomer);
                ps.setDouble(4, grandTotal);
                ps.setInt(5, Session.idUser);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                idJual = rs.getInt(1);
            }

            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String idBarang = modelKeranjang.getValueAt(i, 0).toString();
                double harga    = hargaMap.getOrDefault(idBarang, 0.0);
                int    jumlah   = (int) modelKeranjang.getValueAt(i, 3);
                double subtotal = harga * jumlah;

                try (PreparedStatement psDetail = con.prepareStatement(
                    "INSERT INTO tb_detail_penjualan "
                    + "(id_jual,id_barang,harga_satuan,jumlah_beli,subtotal) "
                    + "VALUES (?,?,?,?,?)")) {
                    psDetail.setInt(1, idJual);
                    psDetail.setString(2, idBarang);
                    psDetail.setDouble(3, harga);
                    psDetail.setInt(4, jumlah);
                    psDetail.setDouble(5, subtotal);
                    psDetail.executeUpdate();
                }

                try (PreparedStatement psStok = con.prepareStatement(
                    "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ?")) {
                    psStok.setInt(1, jumlah);
                    psStok.setString(2, idBarang);
                    psStok.executeUpdate();
                }
            }

            con.commit();
            JOptionPane.showMessageDialog(this,
                "Transaksi berhasil!\nNo Faktur: " + noFaktur
                + "\nTotal: " + FMT.format(grandTotal),
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            bersihForm();
            loadBarang();
            loadRiwayatHariIni();

            JTabbedPane tabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(
                JTabbedPane.class, this);
            if (tabs != null) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    if (tabs.getComponentAt(i) instanceof BarangPanel) {
                        ((BarangPanel) tabs.getComponentAt(i)).loadData();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this,
                "Transaksi GAGAL!\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (con != null) try {
                con.setAutoCommit(true); con.close();
            } catch (SQLException ex) {}
        }
    }

    private int getStokFromDB(String idBarang) {
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT stok FROM tb_barang WHERE id_barang = ?")) {
            ps.setString(1, idBarang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stok");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cek stok: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    public void loadRiwayatHariIni() {
    modelRiwayat.setRowCount(0);
    idJualList.clear();
    String sql = "SELECT p.id_jual, p.no_faktur, p.tgl_transaksi, c.nama_customer, "
               + "p.total_bayar, u.nama_lengkap, p.status "
               + "FROM tb_penjualan p "
               + "JOIN tb_customer c ON p.id_customer = c.id_customer "
               + "JOIN tb_user    u ON p.id_user     = u.id_user "
               + "WHERE p.tgl_transaksi = CURDATE() "
               + "ORDER BY p.id_jual DESC";
    try (Connection con = Koneksi.getKoneksi();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            idJualList.add(rs.getInt("id_jual"));
            String status = rs.getString("status");
            modelRiwayat.addRow(new Object[]{
                rs.getString("no_faktur"),
                rs.getDate("tgl_transaksi"),
                rs.getString("nama_customer"),
                FMT.format(rs.getDouble("total_bayar")),
                rs.getString("nama_lengkap"),
                "batal".equals(status) ? "DIBATALKAN" : "Selesai"
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

            private void batalkanTransaksi() {
            int viewRow = tabelRiwayat.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Pilih transaksi dari tabel Riwayat dulu!");
                return;
            }
            int modelRow = tabelRiwayat.convertRowIndexToModel(viewRow);
            int idJual = idJualList.get(modelRow);
            String statusNow = modelRiwayat.getValueAt(modelRow, 5).toString();

            if (statusNow.equals("DIBATALKAN")) {
                JOptionPane.showMessageDialog(this, "Transaksi ini sudah dibatalkan sebelumnya.");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(this,
                "Batalkan transaksi " + modelRiwayat.getValueAt(modelRow, 0) + "?\n"
                + "Stok barang akan dikembalikan.",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            Connection con = null;
            try {
                con = Koneksi.getKoneksi();
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE tb_penjualan SET status='batal' WHERE id_jual=?")) {
                    ps.setInt(1, idJual);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_barang, jumlah_beli FROM tb_detail_penjualan WHERE id_jual=?")) {
                    ps.setInt(1, idJual);
                    ResultSet rs = ps.executeQuery();
                    try (PreparedStatement psStok = con.prepareStatement(
                            "UPDATE tb_barang SET stok = stok + ? WHERE id_barang=?")) {
                        while (rs.next()) {
                            psStok.setInt(1, rs.getInt("jumlah_beli"));
                            psStok.setString(2, rs.getString("id_barang"));
                            psStok.executeUpdate();
                        }
                    }
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "Transaksi dibatalkan, stok dikembalikan.");
                loadRiwayatHariIni();
                loadBarang();
            } catch (Exception e) {
                try { if (con != null) con.rollback(); } catch (Exception ex) {}
                JOptionPane.showMessageDialog(this, "Gagal membatalkan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (con != null) con.setAutoCommit(true); } catch (Exception ex) {}
            }
}
    private void bersihForm() {
        modelKeranjang.setRowCount(0);
        if (cbCustomer.getItemCount() > 0) cbCustomer.setSelectedIndex(0);
        if (cbBarang.getItemCount()   > 0) cbBarang.setSelectedIndex(0);
        spnJumlah.setValue(1);
        lblGrandTotal.setText("Rp 0");
        grandTotalValue = 0;
        txtUangBayar.setText("");
        lblKembalian.setText("Kembalian: Rp 0");
        lblKembalian.setForeground(new Color(34, 197, 94));
        generateNoFaktur();
        datePicker.setDate(LocalDate.now());
        tabelKeranjang.clearSelection();
        tabelRiwayat.clearSelection();
    }
    @Override
    public void refreshData() {
        loadCustomer();
        loadBarang();
        loadRiwayatHariIni();
    }
    private void hitungKembalian() {
        String input = txtUangBayar.getText().trim();
        double uangBayar = input.isEmpty() ? 0 : Double.parseDouble(input);
        double selisih = uangBayar - grandTotalValue;

        if (selisih >= 0) {
            lblKembalian.setText("Kembalian: " + FMT.format(selisih));
            lblKembalian.setForeground(new Color(34, 197, 94));
        } else {
            lblKembalian.setText("Kurang Bayar: " + FMT.format(Math.abs(selisih)));
            lblKembalian.setForeground(new Color(239, 68, 68));
        }
    }
    private void cetakResi() {
    int viewRow = tabelRiwayat.getSelectedRow();
    if (viewRow < 0) {
        JOptionPane.showMessageDialog(this, "Pilih transaksi dari tabel Riwayat dulu!");
        return;
    }
    int modelRow = tabelRiwayat.convertRowIndexToModel(viewRow);
    int idJual   = idJualList.get(modelRow);
    String noFaktur = modelRiwayat.getValueAt(modelRow, 0).toString();

    String sqlHeader = "SELECT p.no_faktur, p.tgl_transaksi, p.total_bayar, "
                     + "c.nama_customer, u.nama_lengkap "
                     + "FROM tb_penjualan p "
                     + "JOIN tb_customer c ON p.id_customer = c.id_customer "
                     + "JOIN tb_user     u ON p.id_user     = u.id_user "
                     + "WHERE p.id_jual = ?";
    String sqlItems  = "SELECT b.nama_barang, d.jumlah_beli, d.harga_satuan, d.subtotal "
                     + "FROM tb_detail_penjualan d "
                     + "JOIN tb_barang b ON d.id_barang = b.id_barang "
                     + "WHERE d.id_jual = ?";

    try (Connection con = Koneksi.getKoneksi()) {
        String namaCustomer = "", namaKasir = "", tanggal = "";
        double totalBayar = 0;
        try (PreparedStatement ps = con.prepareStatement(sqlHeader)) {
            ps.setInt(1, idJual);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                noFaktur     = rs.getString("no_faktur");
                tanggal      = rs.getDate("tgl_transaksi").toString();
                totalBayar   = rs.getDouble("total_bayar");
                namaCustomer = rs.getString("nama_customer");
                namaKasir    = rs.getString("nama_lengkap");
            }
        }

        java.util.List<Object[]> items = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sqlItems)) {
            ps.setInt(1, idJual);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new Object[]{
                    rs.getString("nama_barang"),
                    rs.getInt("jumlah_beli"),
                    rs.getDouble("harga_satuan"),
                    rs.getDouble("subtotal")
                });
            }
        }

        String fileName    = "Resi-" + noFaktur + ".pdf";
        String desktopPath = System.getProperty("user.home") + "/Desktop/" + fileName;

        Document doc = new Document(PageSize.A5, 30, 30, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(desktopPath));
        doc.open();

        com.lowagie.text.Font fTitle  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fBold   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font fNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.NORMAL);
        com.lowagie.text.Font fSmall  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  8, com.lowagie.text.Font.NORMAL);

        Paragraph title = new Paragraph("TOKO BERKAH JAYA", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Sistem Penjualan", fSmall);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph("================================================", fNormal));
        doc.add(new Paragraph("No. Faktur : " + noFaktur,     fNormal));
        doc.add(new Paragraph("Tanggal    : " + tanggal,      fNormal));
        doc.add(new Paragraph("Customer   : " + namaCustomer, fNormal));
        doc.add(new Paragraph("Kasir      : " + namaKasir,    fNormal));
        doc.add(new Paragraph("------------------------------------------------", fNormal));

        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{4f, 1.5f, 2.5f, 2.5f});
        tbl.setSpacingBefore(4);
        for (String h : new String[]{"Barang", "Qty", "Harga", "Subtotal"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, fBold));
            c.setBorder(com.lowagie.text.Rectangle.BOTTOM);
            c.setPadding(3);
            tbl.addCell(c);
        }
        for (Object[] item : items) {
            PdfPCell c1 = new PdfPCell(new Phrase(item[0].toString(), fNormal));
            PdfPCell c2 = new PdfPCell(new Phrase(item[1].toString(), fNormal));
            PdfPCell c3 = new PdfPCell(new Phrase(FMT.format(item[2]), fNormal));
            PdfPCell c4 = new PdfPCell(new Phrase(FMT.format(item[3]), fNormal));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c1.setBorder(com.lowagie.text.Rectangle.NO_BORDER); c1.setPadding(3);
            c2.setBorder(com.lowagie.text.Rectangle.NO_BORDER); c2.setPadding(3);
            c3.setBorder(com.lowagie.text.Rectangle.NO_BORDER); c3.setPadding(3);
            c4.setBorder(com.lowagie.text.Rectangle.NO_BORDER); c4.setPadding(3);
            tbl.addCell(c1); tbl.addCell(c2); tbl.addCell(c3); tbl.addCell(c4);
        }
        doc.add(tbl);
        doc.add(new Paragraph("------------------------------------------------", fNormal));

        PdfPTable totTbl = new PdfPTable(2);
        totTbl.setWidthPercentage(100);
        totTbl.setSpacingBefore(4);
        String uangBayarStr = txtUangBayar.getText().trim();
        double uangBayar    = uangBayarStr.isEmpty() ? 0 : Double.parseDouble(uangBayarStr);
        double kembalian    = uangBayar - totalBayar;
        String[][] rows = {
            {"TOTAL",     FMT.format(totalBayar)},
            {"BAYAR",     uangBayar > 0 ? FMT.format(uangBayar)             : "-"},
            {"KEMBALIAN", uangBayar > 0 ? FMT.format(Math.max(0, kembalian)) : "-"}
        };
        for (String[] row : rows) {
            PdfPCell cL = new PdfPCell(new Phrase(row[0], fBold));
            PdfPCell cR = new PdfPCell(new Phrase(row[1], fBold));
            cL.setBorder(com.lowagie.text.Rectangle.NO_BORDER); cL.setPadding(3);
            cR.setBorder(com.lowagie.text.Rectangle.NO_BORDER); cR.setPadding(3);
            cR.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totTbl.addCell(cL); totTbl.addCell(cR);
        }
        doc.add(totTbl);
        doc.add(new Paragraph("================================================", fNormal));

        Paragraph footer = new Paragraph("Terima kasih telah berbelanja!", fSmall);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
        doc.close();

        Desktop.getDesktop().open(new File(desktopPath));
        JOptionPane.showMessageDialog(this, "Resi disimpan di Desktop: " + fileName);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal cetak resi: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}