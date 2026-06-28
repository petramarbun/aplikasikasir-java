package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class DatePicker extends JPanel {

    private JTextField txtDate;
    private JButton    btnCalendar;
    private LocalDate  selectedDate;

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SQL_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DatePicker() {
        this(LocalDate.now());
    }

    public DatePicker(LocalDate initialDate) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        selectedDate = initialDate;

        // Field tanggal
        txtDate = new JTextField(initialDate.format(DISPLAY));
        txtDate.setBackground(ThemeManager.BG_SECONDARY);
        txtDate.setForeground(ThemeManager.TEXT_PRIMARY);
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDate.setCaretColor(ThemeManager.ACCENT);
        txtDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 0, ThemeManager.BG_HOVER),
            new EmptyBorder(6, 12, 6, 12)
        ));

        // Tombol kalender
        btnCalendar = new JButton("\uD83D\uDCC5") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeManager.BG_TERTIARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCalendar.setForeground(ThemeManager.ACCENT);
        btnCalendar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCalendar.setFocusPainted(false);
        btnCalendar.setBorderPainted(false);
        btnCalendar.setContentAreaFilled(false);
        btnCalendar.setOpaque(false);
        btnCalendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCalendar.setPreferredSize(new Dimension(36, 36));
        btnCalendar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 1, ThemeManager.BG_HOVER),
            new EmptyBorder(0, 4, 0, 4)
        ));

        add(txtDate,      BorderLayout.CENTER);
        add(btnCalendar,  BorderLayout.EAST);

        btnCalendar.addActionListener(e -> showCalendar());

        // Validasi ketik manual
        txtDate.addActionListener(e -> parseManual());
        txtDate.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { parseManual(); }
        });
    }

    private void parseManual() {
        String txt = txtDate.getText().trim();
        try {
            selectedDate = LocalDate.parse(txt, DISPLAY);
        } catch (Exception ex) {
            try {
                selectedDate = LocalDate.parse(txt, SQL_FMT);
                txtDate.setText(selectedDate.format(DISPLAY));
            } catch (Exception ex2) {
                txtDate.setForeground(ThemeManager.ACCENT_RED);
                txtDate.setToolTipText("Format salah! Gunakan dd/MM/yyyy");
                return;
            }
        }
        txtDate.setForeground(ThemeManager.TEXT_PRIMARY);
        txtDate.setToolTipText(null);
    }

    private void showCalendar() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, Dialog.ModalityType.MODELESS);
        dialog.setUndecorated(true);

        JPanel cal = buildCalendarPanel(dialog);
        dialog.add(cal);
        dialog.pack();

        try {
            // Anchor ke seluruh komponen DatePicker (bukan cuma tombol),
            // rata kiri, muncul di bawah field tanggal -> tidak nabrak sidebar.
            Point loc = this.getLocationOnScreen();
            dialog.setLocation(loc.x, loc.y + this.getHeight() + 2);
        } catch (Exception ignored) {}

        dialog.setVisible(true);
    }

    private JPanel buildCalendarPanel(JDialog dialog) {
        final LocalDate[] view = {YearMonth.from(selectedDate).atDay(1)};

        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(new Color(22, 22, 30));
        main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 70), 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        main.setPreferredSize(new Dimension(290, 320));

        // Header atas
        JLabel lblSelectDate = new JLabel("Select date");
        lblSelectDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSelectDate.setForeground(ThemeManager.TEXT_SECONDARY);

        JLabel[] lblSelected = {new JLabel(selectedDate.format(
            DateTimeFormatter.ofPattern("MMM d, yyyy", new Locale("id"))))};
        lblSelected[0].setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSelected[0].setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel topHeader = new JPanel();
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.setOpaque(false);
        topHeader.add(lblSelectDate);
        topHeader.add(Box.createVerticalStrut(4));
        topHeader.add(lblSelected[0]);

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.BORDER);

        // Navigasi bulan
        JLabel lblMonth = new JLabel();
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonth.setForeground(ThemeManager.TEXT_PRIMARY);

        JButton btnPrev = makeNavBtn("\u2039");
        JButton btnNext = makeNavBtn("\u203A");

        JPanel monthNav = new JPanel(new BorderLayout());
        monthNav.setOpaque(false);
        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navBtns.setOpaque(false);
        navBtns.add(btnPrev); navBtns.add(btnNext);
        monthNav.add(lblMonth, BorderLayout.WEST);
        monthNav.add(navBtns,  BorderLayout.EAST);

        // Grid container
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        final JPanel[] grid = {null};

        Runnable refresh = () -> {
            YearMonth ym = YearMonth.of(view[0].getYear(), view[0].getMonth());
            lblMonth.setText(ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("id")) + " " + ym.getYear());
            if (grid[0] != null) gridContainer.remove(grid[0]);
            grid[0] = buildDayGrid(ym, selectedDate, date -> {
                selectedDate = date;
                txtDate.setText(selectedDate.format(DISPLAY));
                txtDate.setForeground(ThemeManager.TEXT_PRIMARY);
                lblSelected[0].setText(selectedDate.format(
                    DateTimeFormatter.ofPattern("MMM d, yyyy", new Locale("id"))));
                dialog.dispose();
            });
            gridContainer.add(grid[0], BorderLayout.CENTER);
            gridContainer.revalidate();
            gridContainer.repaint();
        };

        btnPrev.addActionListener(e -> { view[0] = view[0].minusMonths(1); refresh.run(); });
        btnNext.addActionListener(e -> { view[0] = view[0].plusMonths(1); refresh.run(); });
        refresh.run();

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(monthNav,     BorderLayout.NORTH);
        center.add(gridContainer, BorderLayout.CENTER);

        main.add(topHeader, BorderLayout.NORTH);
        main.add(sep,       BorderLayout.CENTER);
        main.add(center,    BorderLayout.SOUTH);

        // Klik di luar dialog = tutup
        dialog.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { dialog.dispose(); }
        });

        return main;
    }

    private JPanel buildDayGrid(YearMonth ym, LocalDate selected, Consumer<LocalDate> onSelect) {
        // PENTING: rows=0 (auto), cols=7 (tetap 7 hari/minggu).
        // Kalau rows diisi angka tetap (misalnya 7), Java akan menghitung ULANG
        // jumlah kolom sendiri berdasarkan total komponen / rows, dan itu yang
        // bikin header & tanggal jadi geser/berantakan.
        JPanel panel = new JPanel(new GridLayout(0, 7, 2, 2));
        panel.setOpaque(false);

        String[] headers = {"Sen","Sel","Rab","Kam","Jum","Sab","Min"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(new Color(90, 90, 110));
            panel.add(lbl);
        }

        LocalDate first = ym.atDay(1);
        int startCol = first.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < startCol; i++) panel.add(new JLabel());

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            final LocalDate d = ym.atDay(day);
            boolean isSel   = d.equals(selected);
            boolean isToday = d.equals(today);

            JButton btn = new JButton(String.valueOf(day)) {
                @Override protected void paintComponent(Graphics g) {
                    if (isSel) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(ThemeManager.ACCENT);
                        g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                        g2.dispose();
                    } else if (isToday) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(55, 55, 70));
                        g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", isSel ? Font.BOLD : Font.PLAIN, 11));
            btn.setForeground(isSel ? Color.WHITE : isToday ? ThemeManager.ACCENT : new Color(200, 200, 215));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.addActionListener(e -> onSelect.accept(d));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isSel) btn.setForeground(ThemeManager.ACCENT);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isSel) btn.setForeground(isToday ? ThemeManager.ACCENT : new Color(200, 200, 215));
                }
            });

            panel.add(btn);
        }

        return panel;
    }

    private JButton makeNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(new Color(200, 200, 210));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 28));
        return btn;
    }

    /** Mengembalikan tanggal dalam format SQL (yyyy-MM-dd) */
    public String getSqlDate() {
        return selectedDate.format(SQL_FMT);
    }

    /** Mengembalikan LocalDate yang dipilih */
    public LocalDate getDate() {
        return selectedDate;
    }

    /** Set tanggal secara programatik */
    public void setDate(LocalDate date) {
        this.selectedDate = date;
        txtDate.setText(date.format(DISPLAY));
    }
}