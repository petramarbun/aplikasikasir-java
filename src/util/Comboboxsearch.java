package util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.IllegalComponentStateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Menambahkan kemampuan "cari sambil ngetik" ke JComboBox biasa.
 * Cocok dipakai untuk dropdown yang isinya banyak (kategori, customer,
 * barang) supaya user tinggal ngetik beberapa huruf, bukan scroll panjang.
 *
 * CARA PAKAI:
 *   ComboBoxSearch.enable(cbKategori);   // panggil SEKALI saat combobox dibuat
 *
 *   // setiap kali isi combobox di-reload (removeAllItems + addItem ulang):
 *   cbKategori.removeAllItems();
 *   cbKategori.addItem(...);
 *   ComboBoxSearch.refresh(cbKategori);  // <-- panggil ini setelah reload selesai
 */
public class ComboBoxSearch {

    private static final String KEY_MASTER   = "cbsearch.master";
    private static final String KEY_LISTENER = "cbsearch.listener";

    /** Aktifkan fitur search pada combobox. Panggil sekali saja per combobox. */
    public static <T> void enable(JComboBox<T> comboBox) {
        comboBox.setEditable(true);

        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        editor.setForeground(ThemeManager.TEXT_PRIMARY);
        editor.setBackground(ThemeManager.BG_SECONDARY);
        editor.setCaretColor(ThemeManager.TEXT_PRIMARY);
        editor.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));

        refresh(comboBox);

        DocumentListener filterListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> applyFilter(comboBox, editor)); }
            @Override public void removeUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> applyFilter(comboBox, editor)); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
        editor.getDocument().addDocumentListener(filterListener);
        comboBox.putClientProperty(KEY_LISTENER, filterListener);

        // Kalau popup ditutup tanpa user benar-benar pilih item baru
        // (misal klik di luar), tampilkan lagi full list di belakang layar
        // supaya combobox siap dipakai search dari awal lagi lain kali.
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> restoreFullListKeepSelection(comboBox));
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { }
        });
    }

    /**
     * Panggil ulang setiap kali isi combobox di-reload dari database,
     * supaya daftar "master" yang dipakai untuk filter ikut ter-update.
     */
    public static <T> void refresh(JComboBox<T> comboBox) {
        List<T> master = new ArrayList<>();
        for (int i = 0; i < comboBox.getItemCount(); i++) master.add(comboBox.getItemAt(i));
        comboBox.putClientProperty(KEY_MASTER, master);
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyFilter(JComboBox<T> comboBox, JTextField editor) {
        // Kalau editor ini gak sedang difokus user, berarti perubahan teksnya
        // datang dari proses lain (misal load data ulang dari database),
        // bukan dari user ngetik -> jangan ikut difilter/buka popup.
        if (!editor.isFocusOwner()) return;

        List<T> master = (List<T>) comboBox.getClientProperty(KEY_MASTER);
        if (master == null) return;

        String typed = editor.getText();
        String lower = typed.toLowerCase();

        // Lepas listener kita sendiri SEBELUM mengubah model/teks, supaya
        // efek samping JComboBox (otomatis sinkron teks editor ke item
        // terpilih saat model berubah) TIDAK ikut memicu filter ini lagi
        // -> ini yang sebelumnya menyebabkan loop balik tanpa henti.
        DocumentListener listener = (DocumentListener) comboBox.getClientProperty(KEY_LISTENER);
        if (listener != null) editor.getDocument().removeDocumentListener(listener);
        try {
            DefaultComboBoxModel<T> filteredModel = new DefaultComboBoxModel<>();
            for (T item : master) {
                if (item.toString().toLowerCase().contains(lower)) filteredModel.addElement(item);
            }
            comboBox.setModel(filteredModel);

            if (!editor.getText().equals(typed)) {
                editor.setText(typed);
            }
            editor.setCaretPosition(typed.length());

            if (!typed.isEmpty() && filteredModel.getSize() > 0 && comboBox.isShowing()) {
                try { comboBox.setPopupVisible(true); } catch (IllegalComponentStateException ignored) { }
            } else {
                comboBox.setPopupVisible(false);
            }
        } finally {
            if (listener != null) editor.getDocument().addDocumentListener(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void restoreFullListKeepSelection(JComboBox<T> comboBox) {
        List<T> master = (List<T>) comboBox.getClientProperty(KEY_MASTER);
        if (master == null) return;

        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        DocumentListener listener = (DocumentListener) comboBox.getClientProperty(KEY_LISTENER);

        Object current = comboBox.getSelectedItem();
        if (listener != null) editor.getDocument().removeDocumentListener(listener);
        try {
            DefaultComboBoxModel<T> fullModel = new DefaultComboBoxModel<>();
            for (T item : master) fullModel.addElement(item);
            comboBox.setModel(fullModel);
            if (current != null && master.contains(current)) {
                comboBox.setSelectedItem(current);
            }
        } finally {
            if (listener != null) editor.getDocument().addDocumentListener(listener);
        }
    }
}