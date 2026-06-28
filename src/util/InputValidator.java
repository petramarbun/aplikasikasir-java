package util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class InputValidator {
    public static void namaOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    if (text != null && text.matches("[a-zA-Z\\s]*"))
                        super.insertString(fb, offset, text, attr);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    if (text != null && text.matches("[a-zA-Z\\s.'\\-]*"))
                        super.replace(fb, offset, length, text, attrs);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }
            }
        );
    }

    // ============================================================
    //  2. ANGKA SAJA — untuk telepon, stok, jumlah
    //  maxLength: batas maksimal karakter (0 = tidak terbatas)
    // ============================================================
    public static void angkaOnly(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (text != null && text.matches("[0-9]*")) {
                        if (maxLength <= 0 || (current.length() + text.length()) <= maxLength)
                            super.insertString(fb, offset, text, attr);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    int newLen = current.length() - length + (text == null ? 0 : text.length());
                    if (text != null && text.matches("[0-9]*")) {
                        if (maxLength <= 0 || newLen <= maxLength)
                            super.replace(fb, offset, length, text, attrs);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        );
    }

    // ============================================================
    //  3. ALAMAT — huruf + angka + spasi + . , / - #
    // ============================================================
    public static void alamatOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    if (text != null && text.matches("[a-zA-Z0-9\\s.,/\\-#]*"))
                        super.insertString(fb, offset, text, attr);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    if (text != null && text.matches("[a-zA-Z0-9\\s.,/\\-#]*"))
                        super.replace(fb, offset, length, text, attrs);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }
            }
        );
    }

    // ============================================================
    //  4. KODE / ID — huruf + angka saja (tanpa spasi & simbol)
    //  Contoh field: kode_barang, id_barang, kode_customer
    // ============================================================
    public static void kodeOnly(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (text != null && text.matches("[a-zA-Z0-9]*")) {
                        if (maxLength <= 0 || (current.length() + text.length()) <= maxLength)
                            super.insertString(fb, offset, text.toUpperCase(), attr);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    int newLen = current.length() - length + (text == null ? 0 : text.length());
                    if (text != null && text.matches("[a-zA-Z0-9]*")) {
                        if (maxLength <= 0 || newLen <= maxLength)
                            super.replace(fb, offset, length, text.toUpperCase(), attrs);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        );
    }

    // ============================================================
    //  5. HARGA / DESIMAL — angka + satu titik desimal
    //  Contoh field: harga_jual, harga_beli
    // ============================================================
    public static void hargaOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (isValidHarga(current, offset, 0, text))
                        super.insertString(fb, offset, text, attr);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (isValidHarga(current, offset, length, text))
                        super.replace(fb, offset, length, text, attrs);
                    else
                        Toolkit.getDefaultToolkit().beep();
                }

                private boolean isValidHarga(String current, int offset,
                        int length, String text) {
                    if (text == null || !text.matches("[0-9.]*")) return false;
                    String result = current.substring(0, offset)
                                 + text
                                 + current.substring(offset + length);
                    // Maksimal satu titik desimal
                    return result.matches("[0-9]*\\.?[0-9]*");
                }
            }
        );
    }

    // ============================================================
    //  6. USERNAME — huruf + angka + underscore, tanpa spasi
    // ============================================================
    public static void usernameOnly(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    if (text != null && text.matches("[a-zA-Z0-9_]*")) {
                        if (maxLength <= 0 || (current.length() + text.length()) <= maxLength)
                            super.insertString(fb, offset, text.toLowerCase(), attr);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                    int newLen = current.length() - length + (text == null ? 0 : text.length());
                    if (text != null && text.matches("[a-zA-Z0-9_]*")) {
                        if (maxLength <= 0 || newLen <= maxLength)
                            super.replace(fb, offset, length, text.toLowerCase(), attrs);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        );
    }
    public static void alamatOnly(JTextArea area) {
    ((AbstractDocument) area.getDocument()).setDocumentFilter(
        new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset,
                    String text, AttributeSet attr) throws BadLocationException {
                if (text != null && text.matches("[a-zA-Z0-9\\s.,/\\-#]*"))
                    super.insertString(fb, offset, text, attr);
                else Toolkit.getDefaultToolkit().beep();
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("[a-zA-Z0-9\\s.,/\\-#]*"))
                    super.replace(fb, offset, length, text, attrs);
                else Toolkit.getDefaultToolkit().beep();
            }
        }
    );
}
}