package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Koneksi Database - MySQL (XAMPP)
 * Aplikasi: Toko Berkah Jaya
 */
public class Koneksi {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "toko_berkah_jaya";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";  // default XAMPP kosong

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";

    private static Connection connection = null;

    private Koneksi() {}

     public static Connection getKoneksi() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("✅ Koneksi database berhasil!");
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "Driver MySQL tidak ditemukan!\n" +
                "Pastikan mysql-connector-j.jar sudah ada di Libraries.",
                "Error Driver", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Gagal konek ke database!\n\n" +
                "Pastikan:\n" +
                "  1. XAMPP sudah dibuka\n" +
                "  2. Apache & MySQL sudah START (hijau)\n" +
                "  3. Database 'toko_berkah_jaya' sudah ada\n\n" +
                "Error: " + e.getMessage(),
                "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}