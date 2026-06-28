package ui;

import koneksi.Koneksi;
import util.Session;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblStatus;

    public LoginFrame() {
        setTitle("Login – Toko Berkah Jaya");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(ThemeManager.BG_PRIMARY);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(ThemeManager.BG_PRIMARY);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(ThemeManager.BG_PRIMARY);
        wrapper.setPreferredSize(new Dimension(480, 580));

        // ── Badge ─────────────────────────────────────────
        JLabel badge = new JLabel("SISTEM KASIR") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(249, 115, 22, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(ThemeManager.ACCENT);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ThemeManager.ACCENT);
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(badge);
        wrapper.add(Box.createVerticalStrut(24));

        // ── Judul ─────────────────────────────────────────
        JLabel lblHalo = new JLabel("Halo,");
        lblHalo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblHalo.setForeground(ThemeManager.TEXT_PRIMARY);
        lblHalo.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(lblHalo);

        // Salam real-time
        int jam = java.time.LocalTime.now().getHour();
        String salam;
        if (jam >= 5 && jam < 12)       salam = "Selamat Pagi";
        else if (jam >= 12 && jam < 15) salam = "Selamat Siang";
        else if (jam >= 15 && jam < 18) salam = "Selamat Sore";
        else                             salam = "Selamat Malam";

        JLabel lblSalam = new JLabel(salam) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                // Gradient oranye ke merah
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(249, 115, 22),
                    fm.stringWidth(text), 0, new Color(239, 68, 68));
                g2.setPaint(gp);
                g2.drawString(text, 0, fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                return new Dimension(fm.stringWidth(getText()) + 4,
                    fm.getHeight());
            }
        };
        lblSalam.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblSalam.setForeground(ThemeManager.ACCENT);
        lblSalam.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(lblSalam);
        wrapper.add(Box.createVerticalStrut(8));

        JLabel lblSub = new JLabel("Silakan login untuk melanjutkan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(lblSub);
        wrapper.add(Box.createVerticalStrut(36));

        // ── Card Input ────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_HOVER, 1),
            new EmptyBorder(28, 28, 28, 28)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        // Username
        JLabel lblU = new JLabel("USERNAME");
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblU.setForeground(new Color(100, 100, 100));
        lblU.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblU);
        card.add(Box.createVerticalStrut(8));

        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setBackground(ThemeManager.BG_SECONDARY);
        txtUsername.setForeground(ThemeManager.TEXT_PRIMARY);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setCaretColor(ThemeManager.ACCENT);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 45, 45)),
            new EmptyBorder(10, 14, 10, 14)
        ));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(20));

        // Password
        JLabel lblP = new JLabel("PASSWORD");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblP.setForeground(new Color(100, 100, 100));
        lblP.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblP);
        card.add(Box.createVerticalStrut(8));

        JPanel passRow = new JPanel(new BorderLayout());
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setBackground(ThemeManager.BG_SECONDARY);
        txtPassword.setForeground(ThemeManager.TEXT_PRIMARY);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setCaretColor(ThemeManager.ACCENT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(45, 45, 45)),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JButton btnToggle = new JButton("Lihat");
        btnToggle.setBackground(ThemeManager.BG_SECONDARY);
        btnToggle.setForeground(ThemeManager.ACCENT);
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnToggle.setFocusPainted(false);
        btnToggle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(45, 45, 45)),
            new EmptyBorder(0, 14, 0, 14)
        ));
        btnToggle.setOpaque(true);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.setPreferredSize(new Dimension(75, 46));

        final boolean[] vis = {false};
        btnToggle.addActionListener(e -> {
            vis[0] = !vis[0];
            txtPassword.setEchoChar(vis[0] ? (char) 0 : '●');
            btnToggle.setText(vis[0] ? "Sembunyikan" : "Lihat");
            btnToggle.setPreferredSize(new Dimension(vis[0] ? 118 : 75, 46));
            passRow.revalidate();
        });

        passRow.add(txtPassword, BorderLayout.CENTER);
        passRow.add(btnToggle, BorderLayout.EAST);
        card.add(passRow);

        wrapper.add(card);
        wrapper.add(Box.createVerticalStrut(10));

        // ── Status error ──────────────────────────────────
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(ThemeManager.ACCENT_RED);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(lblStatus);
        wrapper.add(Box.createVerticalStrut(12));

        // ── Tombol Login ──────────────────────────────────
        btnLogin = new JButton("Login") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(249, 115, 22),
                    getWidth(), 0, new Color(239, 68, 68));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setForeground(ThemeManager.TEXT_PRIMARY);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(btnLogin);
        wrapper.add(Box.createVerticalStrut(12));

        // ── Info ──────────────────────────────────────────
        JLabel lblInfo = new JLabel("Lupa kata sandi? Hubungi Administrator");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(ThemeManager.TEXT_MUTED);
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(lblInfo);

        root.add(wrapper);
        add(root);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Username dan password tidak boleh kosong.");
            return;
        }

        String sql = "SELECT u.*, r.nama_role AS level FROM tb_user u "
           + "JOIN tb_role r ON u.id_role = r.id_role "
           + "WHERE u.username = ? AND u.password = ?";
        try (Connection con = Koneksi.getKoneksi();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Session.idUser      = rs.getInt("id_user");
                Session.username    = rs.getString("username");
                Session.namaLengkap = rs.getString("nama_lengkap");
                Session.level       = rs.getString("level");

                JOptionPane.showMessageDialog(this,
                    "Selamat datang, " + Session.namaLengkap + "!",
                    "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);

                dispose();
                new MainFrame().setVisible(true);
            } else {
                lblStatus.setText("Username atau password salah!");
                txtPassword.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}