package ui;

import koneksi.Koneksi;
import util.ThemeManager;
import util.InputValidator;
import util.Refreshable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class CustomerPanel extends JPanel implements Refreshable {

    private JTextField     txtId, txtNama, txtTelepon, txtCari;
    private JTextArea      txtAlamat;
    private JButton        btnTambah, btnUpdate, btnHapus, btnBersih;
    private JTable         tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        loadData();
        
        // Validasi input
       InputValidator.namaOnly(txtNama);
       InputValidator.angkaOnly(txtTelepon, 15);
       InputValidator.alamatOnly(txtAlamat);
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

        JLabel lblTitle = new JLabel("Customer");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Kelola data pelanggan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(20));

        // ID Customer
        sidebar.add(makeLabel("ID CUSTOMER"));
        sidebar.add(Box.createVerticalStrut(6));
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleDisabledField(txtId);
        sidebar.add(txtId);
        sidebar.add(Box.createVerticalStrut(12));

        // Nama
        sidebar.add(makeLabel("NAMA CUSTOMER"));
        sidebar.add(Box.createVerticalStrut(6));
        txtNama = new JTextField();
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtNama);
        sidebar.add(txtNama);
        sidebar.add(Box.createVerticalStrut(12));

        // Telepon
        sidebar.add(makeLabel("TELEPON"));
        sidebar.add(Box.createVerticalStrut(6));
        txtTelepon = new JTextField();
        txtTelepon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtTelepon.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtTelepon);
        sidebar.add(txtTelepon);
        sidebar.add(Box.createVerticalStrut(12));

        // Alamat
        sidebar.add(makeLabel("ALAMAT"));
        sidebar.add(Box.createVerticalStrut(6));
        txtAlamat = new JTextArea(3, 1);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.setBackground(ThemeManager.BG_SECONDARY);
        txtAlamat.setForeground(ThemeManager.TEXT_PRIMARY);
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlamat.setCaretColor(ThemeManager.ACCENT);
        txtAlamat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_HOVER),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JScrollPane alamatScroll = new JScrollPane(txtAlamat);
        alamatScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.BG_HOVER));
        alamatScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        alamatScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(alamatScroll);
        sidebar.add(Box.createVerticalStrut(24));

        // Tombol
        btnTambah = makeBtn("Tambah Customer", ThemeManager.ACCENT);
        btnUpdate = makeBtn("Update",           new Color(234, 179, 8));
        btnHapus  = makeBtn("Hapus",            new Color(239, 68, 68));
        btnBersih = makeBtn("Bersih",           ThemeManager.ACCENT_BLUE);

        sidebar.add(btnTambah); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnUpdate); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnHapus);  sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnBersih);

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

        JLabel lblTableTitle = new JLabel("Data Customer");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        titleRow.add(lblTableTitle);

        JLabel lblTableSub = new JLabel("Daftar semua pelanggan");
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
            new String[]{"ID Customer","Nama","Alamat","Telepon"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabel);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(200);

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
            txtAlamat.setText(model.getValueAt(modelRow, 2).toString());
            txtTelepon.setText(model.getValueAt(modelRow, 3).toString());
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

    public void loadData() {
        model.setRowCount(0);
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM tb_customer ORDER BY id_customer")) {
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getString("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("alamat"),
                    rs.getString("telepon")
                });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validasi() {
        if (txtNama.getText().trim().isEmpty())  { JOptionPane.showMessageDialog(this,"Nama Customer wajib diisi!"); return false; }
        return true;
    }

    private void tambah() {
    if (!validasi()) return;
    String idBaru = generateIdCustomer();
    try (Connection con = Koneksi.getKoneksi();
         PreparedStatement ps = con.prepareStatement(
             "INSERT INTO tb_customer(id_customer, nama_customer, alamat, telepon) VALUES(?,?,?,?)")) {
        ps.setString(1, idBaru);
        ps.setString(2, txtNama.getText().trim());
        ps.setString(3, txtAlamat.getText().trim());
        ps.setString(4, txtTelepon.getText().trim());
        ps.executeUpdate();
        JOptionPane.showMessageDialog(this, "Customer ditambahkan! ID: " + idBaru);
        bersihForm(); loadData();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void update() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih customer dari tabel!"); return;
        }
        if (!validasi()) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE tb_customer SET nama_customer=?,alamat=?,telepon=? "
                 + "WHERE id_customer=?")) {
            ps.setString(1, txtNama.getText().trim());
            ps.setString(2, txtAlamat.getText().trim());
            ps.setString(3, txtTelepon.getText().trim());
            ps.setString(4, txtId.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer berhasil diupdate.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih customer dari tabel!"); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin hapus customer ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM tb_customer WHERE id_customer=?")) {
            ps.setString(1, txtId.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer berhasil dihapus.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus — customer masih memiliki riwayat transaksi.",
                "Tidak Bisa Hapus", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bersihForm() {
        txtId.setText(""); txtNama.setText("");
        txtAlamat.setText(""); txtTelepon.setText("");
        txtCari.setText("");
        tabel.clearSelection();
    }
    @Override
    public void refreshData() {
        loadData();
    }
    
    private String generateIdCustomer() {
    try (Connection con = Koneksi.getKoneksi();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(
             "SELECT MAX(CAST(SUBSTRING(id_customer, 5) AS UNSIGNED)) " +
             "FROM tb_customer WHERE id_customer REGEXP '^CUST[0-9]+$'")) {
        if (rs.next()) {
            return String.format("CUST%03d", rs.getInt(1) + 1);
        }
    } catch (Exception e) { }
    return "CUST001";
}
}