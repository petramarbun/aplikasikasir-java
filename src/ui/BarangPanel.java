package ui;

import koneksi.Koneksi;
import util.ThemeManager;
import util.Session;
import util.InputValidator;
import util.Refreshable;
import util.ComboBoxSearch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class BarangPanel extends JPanel implements Refreshable {

    private JTextField     txtId, txtNama, txtSatuan, txtHarga, txtStok, txtCari;
    private JComboBox<String> cbKategori;
    private JButton        btnTambah, btnUpdate, btnHapus, btnBersih;
    private JTable         tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private java.util.List<Integer> idKategoriList = new java.util.ArrayList<>();

    public BarangPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        loadKategori();
        loadData();
        
         // Validasi input
        InputValidator.kodeOnly(txtId, 10);
        InputValidator.alamatOnly(txtNama);
        InputValidator.angkaOnly(txtStok, 6);
        InputValidator.hargaOnly(txtHarga);
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

        sidebar.add(makeBadge("MANAJEMEN DATA"));
        sidebar.add(Box.createVerticalStrut(16));

        JLabel lblTitle = new JLabel("Barang");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Kelola data produk toko");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(20));

        // ID Barang
        sidebar.add(makeLabel("ID BARANG"));
        sidebar.add(Box.createVerticalStrut(6));
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleDisabledField(txtId);
        sidebar.add(txtId);
        sidebar.add(Box.createVerticalStrut(12));

        // Nama Barang
        sidebar.add(makeLabel("NAMA BARANG"));
        sidebar.add(Box.createVerticalStrut(6));
        txtNama = new JTextField();
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtNama);
        sidebar.add(txtNama);
        sidebar.add(Box.createVerticalStrut(12));

        // Kategori
        sidebar.add(makeLabel("KATEGORI"));
        sidebar.add(Box.createVerticalStrut(6));
        cbKategori = new JComboBox<>();
        ThemeManager.styleComboBox(cbKategori);
        cbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cbKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbKategori.setBackground(ThemeManager.BG_SECONDARY);
        cbKategori.setForeground(ThemeManager.TEXT_PRIMARY);
        cbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbKategori.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.BG_SECONDARY);
                setForeground(isSelected ? Color.WHITE : ThemeManager.TEXT_PRIMARY);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBorder(new EmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        sidebar.add(cbKategori);
        ComboBoxSearch.enable(cbKategori);
        sidebar.add(Box.createVerticalStrut(12));

        // Satuan
        sidebar.add(makeLabel("SATUAN"));
        sidebar.add(Box.createVerticalStrut(6));
        txtSatuan = new JTextField();
        txtSatuan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtSatuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtSatuan);
        sidebar.add(txtSatuan);
        sidebar.add(Box.createVerticalStrut(12));

        // Harga + Stok
        JPanel rowHS = new JPanel(new GridLayout(1, 2, 10, 0));
        rowHS.setOpaque(false);
        rowHS.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        rowHS.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel colH = new JPanel();
        colH.setLayout(new BoxLayout(colH, BoxLayout.Y_AXIS));
        colH.setOpaque(false);
        colH.add(makeLabel("HARGA JUAL"));
        colH.add(Box.createVerticalStrut(6));
        txtHarga = new JTextField();
        txtHarga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        styleField(txtHarga);
        colH.add(txtHarga);

        JPanel colS = new JPanel();
        colS.setLayout(new BoxLayout(colS, BoxLayout.Y_AXIS));
        colS.setOpaque(false);
        colS.add(makeLabel("STOK"));
        colS.add(Box.createVerticalStrut(6));
        txtStok = new JTextField();
        txtStok.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        styleField(txtStok);
        colS.add(txtStok);

        rowHS.add(colH);
        rowHS.add(colS);
        sidebar.add(rowHS);
        sidebar.add(Box.createVerticalStrut(24));

        btnTambah = makeBtn("Tambah Barang", ThemeManager.ACCENT);
        btnUpdate = makeBtn("Update",         new Color(234, 179, 8));
        btnHapus  = makeBtn("Hapus",          new Color(239, 68, 68));
        btnBersih = makeBtn("Bersih",         ThemeManager.ACCENT_BLUE);

        sidebar.add(btnTambah); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnUpdate); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnHapus);  sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnBersih);
        
        // Sembunyikan tombol CRUD untuk Petugas
        if ("Petugas".equals(Session.level)) {
            btnTambah.setVisible(false);
            btnUpdate.setVisible(false);
            btnHapus.setVisible(false);
            btnBersih.setVisible(false);
            txtId.setEditable(false);
            txtNama.setEditable(false);
            txtSatuan.setEditable(false);
            txtHarga.setEditable(false);
            txtStok.setEditable(false);
            cbKategori.setEnabled(false);

            // Tambah label info
            JLabel lblReadOnly = new JLabel("Mode: Lihat Saja");
            lblReadOnly.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblReadOnly.setForeground(new Color(234, 179, 8));
            lblReadOnly.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(Box.createVerticalStrut(12));
            sidebar.add(lblReadOnly);
        }

        // ── Area Tabel (kanan) ────────────────────────────
        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setBackground(ThemeManager.BG_PRIMARY);
        tableArea.setBorder(new EmptyBorder(32, 28, 32, 32));

        // Header + Search
        JPanel tableHeader = new JPanel(new BorderLayout(12, 0));
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.Y_AXIS));
        titleRow.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Data Barang");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        titleRow.add(lblTableTitle);

        JLabel lblTableSub = new JLabel("Daftar semua produk yang tersedia");
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

        // Tabel
        model = new DefaultTableModel(
            new String[]{"ID","Nama Barang","Kategori","Satuan","Harga Jual","Stok"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 4) return Double.class;
                if (c == 5) return Integer.class;
                return String.class;
            }
        };
        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabel);

        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabel);
        ThemeManager.styleScrollPane(scroll);

        tableArea.add(tableHeader, BorderLayout.NORTH);
        tableArea.add(scroll,      BorderLayout.CENTER);

        add(sidebar,   BorderLayout.WEST);
        add(tableArea, BorderLayout.CENTER);

        // ── Events ────────────────────────────────────────
        btnTambah.addActionListener(e -> tambah());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e  -> hapus());
        btnBersih.addActionListener(e -> bersihForm());

        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        tabel.getSelectionModel().addListSelectionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row < 0) { bersihForm(); return; }
            int modelRow = tabel.convertRowIndexToModel(row);
            txtId.setText(model.getValueAt(modelRow, 0).toString());
            txtNama.setText(model.getValueAt(modelRow, 1).toString());
            String katNama = model.getValueAt(modelRow, 2).toString();
            for (int i = 0; i < cbKategori.getItemCount(); i++) {
                if (cbKategori.getItemAt(i).contains(katNama)) {
                    cbKategori.setSelectedIndex(i); break;
                }
            }
            txtSatuan.setText(model.getValueAt(modelRow, 3).toString());
            txtHarga.setText(model.getValueAt(modelRow, 4).toString());
            txtStok.setText(model.getValueAt(modelRow, 5).toString());
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

    private void styleDisabledField(JTextField f) {
        f.setBackground(ThemeManager.BG_SECONDARY);
        f.setForeground(new Color(60, 60, 60));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_SECONDARY),
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

    public void loadKategori() {
        cbKategori.removeAllItems();
        idKategoriList.clear();
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM tb_kategori ORDER BY id_kategori")) {
            while (rs.next()) {
                idKategoriList.add(rs.getInt("id_kategori"));
                cbKategori.addItem(rs.getInt("id_kategori") + " – " +
                    rs.getString("nama_kategori"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        ComboBoxSearch.refresh(cbKategori);
    }

    public void loadData() {
        model.setRowCount(0);
        String sql = "SELECT b.id_barang, b.nama_barang, k.nama_kategori, "
                   + "b.satuan, b.harga_jual, b.stok "
                   + "FROM tb_barang b JOIN tb_kategori k "
                   + "ON b.id_kategori = k.id_kategori ORDER BY b.id_barang";
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getString("satuan"),
                    rs.getDouble("harga_jual"),
                    rs.getInt("stok")
                });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedKategoriId() {
        Object sel = cbKategori.getSelectedItem();
        if (sel == null) return -1;
        try {
            return Integer.parseInt(sel.toString().split(" – ")[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean validasiForm() {
        if (txtNama.getText().trim().isEmpty())   { JOptionPane.showMessageDialog(this,"Nama Barang wajib diisi!"); return false; }
        if (!txtNama.getText().trim().matches("[a-zA-Z0-9 .,/-]+")) {
            JOptionPane.showMessageDialog(this,"Nama Barang tidak boleh mengandung simbol aneh!");
            return false;
        }
        if (cbKategori.getSelectedIndex() < 0)   { JOptionPane.showMessageDialog(this,"Pilih kategori!");          return false; }
        if (txtSatuan.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this,"Satuan wajib diisi!");      return false; }
        if (!txtSatuan.getText().trim().matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this,"Satuan cuma boleh huruf, contoh: pcs, botol, karung");
            return false;
        }
        try { Double.parseDouble(txtHarga.getText()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Harga harus berupa angka!"); return false; }
        try { Integer.parseInt(txtStok.getText()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Stok harus berupa angka bulat!"); return false; }
        return true;
    }

    private void tambah() {
    if (!validasiForm()) return;
    String idBaru = generateIdBarang();
    try (Connection con = Koneksi.getKoneksi();
         PreparedStatement ps = con.prepareStatement(
             "INSERT INTO tb_barang(id_barang, nama_barang, id_kategori, satuan, harga_jual, stok) VALUES(?,?,?,?,?,?)")) {
        ps.setString(1, idBaru);
        ps.setString(2, txtNama.getText().trim());
        ps.setInt(3, getSelectedKategoriId());
        ps.setString(4, txtSatuan.getText().trim());
        ps.setDouble(5, Double.parseDouble(txtHarga.getText().trim()));
        ps.setInt(6, Integer.parseInt(txtStok.getText().trim()));
        ps.executeUpdate();
        JOptionPane.showMessageDialog(this, "Barang ditambahkan! ID: " + idBaru);
        bersihForm(); loadData();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void update() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang dari tabel!"); return;
        }
        if (!validasiForm()) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE tb_barang SET id_kategori=?,nama_barang=?,"
                 + "satuan=?,harga_jual=?,stok=? WHERE id_barang=?")) {
            ps.setInt(1, getSelectedKategoriId());
            ps.setString(2, txtNama.getText().trim());
            ps.setString(3, txtSatuan.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtHarga.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStok.getText().trim()));
            ps.setString(6, txtId.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Barang berhasil diupdate.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang dari tabel!"); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin hapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM tb_barang WHERE id_barang=?")) {
            ps.setString(1, txtId.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Barang berhasil dihapus.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus — barang masih terkait dengan transaksi.",
                "Tidak Bisa Hapus", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bersihForm() {
        txtId.setText(""); txtNama.setText("");
        txtSatuan.setText(""); txtHarga.setText("");
        txtStok.setText(""); txtCari.setText("");
        if (cbKategori.getItemCount() > 0) cbKategori.setSelectedIndex(0);
        tabel.clearSelection();
    }
    @Override
    public void refreshData() {
        loadKategori();
        loadData();
    }
    
    private String generateIdBarang() {
    try (Connection con = Koneksi.getKoneksi();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(
             "SELECT MAX(CAST(SUBSTRING(id_barang, 4) AS UNSIGNED)) " +
             "FROM tb_barang WHERE id_barang REGEXP '^BRG[0-9]+$'")) {
        if (rs.next()) {
            return String.format("BRG%03d", rs.getInt(1) + 1);
        }
    } catch (Exception e) { }
    return "BRG001";
        }
}