package ui;

import koneksi.Koneksi;
import util.Session;
import util.ThemeManager;
import util.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class UserPanel extends JPanel {

    private JTextField     txtId, txtUsername, txtNamaLengkap, txtPassword, txtCari;
    private JComboBox<String> cbLevel;
    private JButton        btnTambah, btnUpdate, btnHapus, btnBersih;
    private JTable         tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public UserPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG_PRIMARY);
        ThemeManager.enableClickAwayFocus(this);
        buildUI();
        loadData();
        // Validasi input
        InputValidator.usernameOnly(txtUsername, 50);
        InputValidator.namaOnly(txtNamaLengkap);
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

        sidebar.add(makeBadge("MANAJEMEN USER"));
        sidebar.add(Box.createVerticalStrut(16));

        JLabel lblTitle = new JLabel("User");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);

        JLabel lblSub = new JLabel("Kelola akun pengguna sistem");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(20));

        // ID (auto, read-only)
        sidebar.add(makeLabel("ID USER"));
        sidebar.add(Box.createVerticalStrut(6));
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtId.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleDisabledField(txtId);
        sidebar.add(txtId);
        sidebar.add(Box.createVerticalStrut(12));

        // Username
        sidebar.add(makeLabel("USERNAME"));
        sidebar.add(Box.createVerticalStrut(6));
        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtUsername);
        sidebar.add(txtUsername);
        sidebar.add(Box.createVerticalStrut(12));

        // Password
        sidebar.add(makeLabel("PASSWORD"));
        sidebar.add(Box.createVerticalStrut(6));
        txtPassword = new JTextField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtPassword);
        sidebar.add(txtPassword);
        sidebar.add(Box.createVerticalStrut(12));

        // Nama Lengkap
        sidebar.add(makeLabel("NAMA LENGKAP"));
        sidebar.add(Box.createVerticalStrut(6));
        txtNamaLengkap = new JTextField();
        txtNamaLengkap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtNamaLengkap.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleField(txtNamaLengkap);
        sidebar.add(txtNamaLengkap);
        sidebar.add(Box.createVerticalStrut(12));

        // Level
        sidebar.add(makeLabel("LEVEL"));
        sidebar.add(Box.createVerticalStrut(6));
        cbLevel = new JComboBox<>(new String[]{"Admin", "Petugas"});
        cbLevel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cbLevel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbLevel.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(java.awt.Graphics g,
                    java.awt.Rectangle bounds, boolean hasFocus) {
                g.setColor(ThemeManager.BG_SECONDARY);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        cbLevel.setBackground(ThemeManager.BG_SECONDARY);
        cbLevel.setForeground(ThemeManager.TEXT_PRIMARY);
        cbLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLevel.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(true);
                if (index == -1) {
                    // Area display combo (teks item terpilih saat combo tertutup)
                    setBackground(ThemeManager.BG_SECONDARY);
                    setForeground(ThemeManager.TEXT_PRIMARY);
                } else {
                    setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.BG_SECONDARY);
                    setForeground(isSelected ? Color.WHITE : ThemeManager.TEXT_PRIMARY);
                }
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBorder(new EmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        sidebar.add(cbLevel);
        sidebar.add(Box.createVerticalStrut(28));

        // Tombol
        btnTambah = makeBtn("Tambah User",  ThemeManager.ACCENT);
        btnUpdate = makeBtn("Update",        new Color(234, 179, 8));
        btnHapus  = makeBtn("Hapus",         new Color(239, 68, 68));
        btnBersih = makeBtn("Bersih",        ThemeManager.ACCENT_BLUE);

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

        JLabel lblTableTitle = new JLabel("Data User");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        titleRow.add(lblTableTitle);

        JLabel lblTableSub = new JLabel("Daftar semua pengguna sistem");
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
            new String[]{"ID","Username","Nama Lengkap","Level"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
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
            txtUsername.setText(model.getValueAt(modelRow, 1).toString());
            txtNamaLengkap.setText(model.getValueAt(modelRow, 2).toString());
            txtPassword.setText("");
            cbLevel.setSelectedItem(model.getValueAt(modelRow, 3).toString());
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
                 "SELECT u.id_user, u.username, u.nama_lengkap, r.nama_role AS level "
                  + "FROM tb_user u JOIN tb_role r ON u.id_role = r.id_role ORDER BY u.id_user")) {
            while (rs.next())
                model.addRow(new Object[]{
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("nama_lengkap"),
                    rs.getString("level")
                });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validasi() {
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username wajib diisi!"); return false;
        }
        if (txtNamaLengkap.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama lengkap wajib diisi!"); return false;
        }
        return true;
    }

    private void tambah() {
        if (!validasi()) return;
        if (txtPassword.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password wajib diisi untuk user baru!");
            return;
        }
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO tb_user (username,password,nama_lengkap,id_role) VALUES(?,?,?,?)")) {
            ps.setString(1, txtUsername.getText().trim());
            ps.setString(2, txtPassword.getText().trim());
            ps.setString(3, txtNamaLengkap.getText().trim());
            String selectedLevel = cbLevel.getSelectedItem().toString();
            int idRole = selectedLevel.equals("Admin") ? 1 : 2;
            ps.setInt(4, idRole);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil ditambahkan.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Username sudah dipakai!",
                "Duplikasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void update() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih user dari tabel!"); return;
        }
        if (!validasi()) return;

        // Cegah edit user yang sedang login
        if (Integer.parseInt(txtId.getText()) == Session.idUser &&
            !cbLevel.getSelectedItem().toString().equals(Session.level)) {
            JOptionPane.showMessageDialog(this,
                "Tidak bisa mengubah level akun yang sedang digunakan!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = Koneksi.getKoneksi()) {
            String sql;
            if (!txtPassword.getText().trim().isEmpty()) {
               sql = "UPDATE tb_user SET username=?,password=?,nama_lengkap=?,id_role=? WHERE id_user=?";
            } else {
                sql = "UPDATE tb_user SET username=?,nama_lengkap=?,id_role=? WHERE id_user=?";
            }

            PreparedStatement ps = con.prepareStatement(sql);
            if (!txtPassword.getText().trim().isEmpty()) {
                ps.setString(1, txtUsername.getText().trim());
                ps.setString(2, txtPassword.getText().trim());
                ps.setString(3, txtNamaLengkap.getText().trim());
                int idRole = cbLevel.getSelectedItem().toString().equals("Admin") ? 1 : 2;
                ps.setInt(4, idRole);
                ps.setInt(5, Integer.parseInt(txtId.getText()));
            } else {
                ps.setString(1, txtUsername.getText().trim());
                ps.setString(2, txtNamaLengkap.getText().trim());
                int idRole2 = cbLevel.getSelectedItem().toString().equals("Admin") ? 1 : 2;
                ps.setInt(3, idRole2);
                ps.setInt(4, Integer.parseInt(txtId.getText()));
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil diupdate.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih user dari tabel!"); return;
        }
        if (Integer.parseInt(txtId.getText()) == Session.idUser) {
            JOptionPane.showMessageDialog(this,
                "Tidak bisa menghapus akun yang sedang digunakan!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin hapus user ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM tb_user WHERE id_user=?")) {
            ps.setInt(1, Integer.parseInt(txtId.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
            bersihForm(); loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus — user masih memiliki riwayat transaksi.",
                "Tidak Bisa Hapus", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bersihForm() {
        txtId.setText(""); txtUsername.setText("");
        txtPassword.setText(""); txtNamaLengkap.setText("");
        txtCari.setText("");
        cbLevel.setSelectedIndex(0);
        tabel.clearSelection();
    }
}