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

public class KategoriPanel extends JPanel implements Refreshable {

    private JTextField        txtId, txtNama, txtCari;
    private JButton           btnTambah, btnUpdate, btnHapus, btnBersih;
    private JTable            tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public KategoriPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        loadData();
        InputValidator.namaOnly(txtNama);
    }

    private void buildUI() {
        // ── Sidebar (kiri) ────────────────────────────────
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

        JLabel lblTitle = new JLabel("Kategori");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Kelola data kategori barang");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(32));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(24));

        // ID Kategori
        sidebar.add(makeLabel("ID KATEGORI"));
        sidebar.add(Box.createVerticalStrut(6));
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleDisabledField(txtId);
        sidebar.add(txtId);
        sidebar.add(Box.createVerticalStrut(16));

        // Nama Kategori
        sidebar.add(makeLabel("NAMA KATEGORI"));
        sidebar.add(Box.createVerticalStrut(6));
        txtNama = new JTextField();
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtNama);
        sidebar.add(txtNama);
        sidebar.add(Box.createVerticalStrut(28));

        // Tombol
        btnTambah = makeBtn("Tambah Kategori", ThemeManager.ACCENT);
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

        JLabel lblTableTitle = new JLabel("Data Kategori");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        titleRow.add(lblTableTitle);

        JLabel lblTableSub = new JLabel("Daftar semua kategori yang tersedia");
        lblTableSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSub.setForeground(ThemeManager.TEXT_MUTED);
        titleRow.add(lblTableSub);

        // Search box
        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(220, 40));

        JLabel lblSearch = new JLabel("🔍");
        lblSearch.setForeground(ThemeManager.TEXT_SECONDARY);
        lblSearch.setBorder(new EmptyBorder(0, 10, 0, 4));

        txtCari = new JTextField();
        txtCari.setBackground(ThemeManager.BG_SECONDARY);
        txtCari.setForeground(ThemeManager.TEXT_PRIMARY);
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCari.setCaretColor(ThemeManager.ACCENT);
        txtCari.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BORDER),
            new EmptyBorder(6, 8, 6, 8)
        ));
        txtCari.putClientProperty("JTextField.placeholderText", "Cari kategori...");

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(ThemeManager.BG_SECONDARY);
        searchWrapper.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));
        searchWrapper.add(lblSearch, BorderLayout.WEST);
        searchWrapper.add(txtCari,   BorderLayout.CENTER);

        searchPanel.add(searchWrapper, BorderLayout.CENTER);

        tableHeader.add(titleRow,     BorderLayout.WEST);
        tableHeader.add(searchPanel,  BorderLayout.EAST);

        // Tabel
        model = new DefaultTableModel(
            new String[]{"ID Kategori", "Nama Kategori"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeManager.styleTable(tabel);

        // ── TableRowSorter untuk search ───────────────────
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

        // Search listener — filter saat mengetik
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        tabel.getSelectionModel().addListSelectionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row >= 0) {
                int modelRow = tabel.convertRowIndexToModel(row);
                txtId.setText(model.getValueAt(modelRow, 0).toString());
                txtNama.setText(model.getValueAt(modelRow, 1).toString());
            } else {
                bersihForm();
            }
        });
    }

    private void filter() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
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

    private void loadData() {
        model.setRowCount(0);
        try (Connection con = Koneksi.getKoneksi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM tb_kategori ORDER BY id_kategori")) {
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getInt("id_kategori"),
                    rs.getString("nama_kategori")
                });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tambah() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori wajib diisi!");
            return;
        }
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO tb_kategori(nama_kategori) VALUES(?)")) {
            ps.setString(1, nama);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kategori terlebih dahulu!");
            return;
        }
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE tb_kategori SET nama_kategori=? WHERE id_kategori=?")) {
            ps.setString(1, txtNama.getText().trim());
            ps.setInt(2, Integer.parseInt(txtId.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kategori berhasil diupdate.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kategori terlebih dahulu!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin hapus kategori ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM tb_kategori WHERE id_kategori=?")) {
            ps.setInt(1, Integer.parseInt(txtId.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus — kategori masih dipakai oleh barang.\n",
                "Tidak Bisa Hapus", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bersihForm() {
        txtId.setText("");
        txtNama.setText("");
        txtCari.setText("");
        tabel.clearSelection();
    }
    @Override
    public void refreshData() {
        loadData();
    }
}