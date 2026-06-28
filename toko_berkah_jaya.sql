-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 28 Jun 2026 pada 05.34
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `toko_berkah_jaya`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_barang`
--

CREATE TABLE `tb_barang` (
  `id_barang` varchar(20) NOT NULL,
  `nama_barang` varchar(150) NOT NULL,
  `id_kategori` int(11) DEFAULT NULL,
  `satuan` varchar(20) DEFAULT 'pcs',
  `harga_jual` decimal(15,2) NOT NULL DEFAULT 0.00,
  `stok` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_barang`
--

INSERT INTO `tb_barang` (`id_barang`, `nama_barang`, `id_kategori`, `satuan`, `harga_jual`, `stok`) VALUES
('BRG001', 'Aqua Botol 600ml', 1, 'botol', 3500.00, 99),
('BRG002', 'Teh Pucuk Harum', 1, 'botol', 4000.00, 79),
('BRG003', 'Indomie Goreng', 2, 'bungkus', 3000.00, 150),
('BRG004', 'Roti Tawar', 2, 'bungkus', 12000.00, 40),
('BRG005', 'Beras Premium 5kg', 3, 'karung', 65000.00, 30),
('BRG006', 'Minyak Goreng 1L', 3, 'botol', 18000.00, 50),
('BRG007', 'Sabun Mandi', 4, 'batang', 4500.00, 60),
('BRG008', 'Pasta Gigi', 4, 'tube', 8500.00, 45),
('BRG009', 'Buku Tulis 38 Lembar', 5, 'pcs', 3500.00, 100),
('BRG010', 'Pulpen Standard', 5, 'pcs', 2500.00, 200),
('BRG011', 'Charger HP Universal', 6, 'pcs', 35000.00, 25),
('BRG012', 'Earphone Kabel', 6, 'pcs', 25000.00, 20);

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_customer`
--

CREATE TABLE `tb_customer` (
  `id_customer` varchar(20) NOT NULL,
  `nama_customer` varchar(100) NOT NULL,
  `alamat` text DEFAULT NULL,
  `telepon` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_customer`
--

INSERT INTO `tb_customer` (`id_customer`, `nama_customer`, `alamat`, `telepon`) VALUES
('CUST001', 'Yoga Pratama', 'Jl. Merdeka No. 10', '081234567890'),
('CUST002', 'Siti Rahma', 'Jl. Anggrek No. 5', '081298765432'),
('CUST003', 'Budi Santoso', 'Jl. Mawar No. 2', '081387654321');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_detail_penjualan`
--

CREATE TABLE `tb_detail_penjualan` (
  `id_detail` int(11) NOT NULL,
  `id_jual` int(11) NOT NULL,
  `id_barang` varchar(20) NOT NULL,
  `harga_satuan` decimal(15,2) NOT NULL,
  `jumlah_beli` int(11) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_detail_penjualan`
--

INSERT INTO `tb_detail_penjualan` (`id_detail`, `id_jual`, `id_barang`, `harga_satuan`, `jumlah_beli`, `subtotal`) VALUES
(1, 1, 'BRG001', 3500.00, 1, 3500.00),
(2, 1, 'BRG002', 4000.00, 1, 4000.00);

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_kategori`
--

CREATE TABLE `tb_kategori` (
  `id_kategori` int(11) NOT NULL,
  `nama_kategori` varchar(100) NOT NULL,
  `deskripsi` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_kategori`
--

INSERT INTO `tb_kategori` (`id_kategori`, `nama_kategori`, `deskripsi`) VALUES
(1, 'Minuman', 'Air mineral, jus, minuman kemasan'),
(2, 'Makanan', 'Makanan ringan, mie instan, snack'),
(3, 'Sembako', 'Beras, minyak, gula, tepung'),
(4, 'Kebersihan', 'Sabun, deterjen, pembersih rumah'),
(5, 'Alat Tulis', 'Buku, pulpen, perlengkapan sekolah/kantor'),
(6, 'Elektronik', 'Aksesoris HP, charger, earphone'),
(7, 'Pakaian', NULL),
(8, 'Kopi', NULL),
(9, 'Pewangi', NULL);

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_penjualan`
--

CREATE TABLE `tb_penjualan` (
  `id_jual` int(11) NOT NULL,
  `no_faktur` varchar(30) NOT NULL,
  `tgl_transaksi` date NOT NULL,
  `id_customer` varchar(20) DEFAULT NULL,
  `total_bayar` decimal(15,2) NOT NULL DEFAULT 0.00,
  `id_user` int(11) NOT NULL,
  `status` enum('selesai','batal') NOT NULL DEFAULT 'selesai'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_penjualan`
--

INSERT INTO `tb_penjualan` (`id_jual`, `no_faktur`, `tgl_transaksi`, `id_customer`, `total_bayar`, `id_user`, `status`) VALUES
(1, 'FAK-20260623-119', '2026-06-23', 'CUST001', 7500.00, 1, 'selesai');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_role`
--

CREATE TABLE `tb_role` (
  `id_role` int(11) NOT NULL,
  `nama_role` varchar(50) NOT NULL,
  `deskripsi` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_role`
--

INSERT INTO `tb_role` (`id_role`, `nama_role`, `deskripsi`) VALUES
(1, 'Admin', 'Administrator, akses penuh ke semua fitur'),
(2, 'Petugas', 'Kasir, akses transaksi & lihat produk');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_user`
--

CREATE TABLE `tb_user` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) NOT NULL,
  `id_role` int(11) NOT NULL,
  `status` enum('aktif','nonaktif') DEFAULT 'aktif',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_user`
--

INSERT INTO `tb_user` (`id_user`, `username`, `password`, `nama_lengkap`, `id_role`, `status`, `created_at`) VALUES
(1, 'admin', 'admin123', 'Administrator', 1, 'aktif', '2026-06-21 13:20:25'),
(2, 'kasir1', 'kasir123', 'Kasir Satu', 2, 'aktif', '2026-06-21 13:20:25');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `tb_barang`
--
ALTER TABLE `tb_barang`
  ADD PRIMARY KEY (`id_barang`),
  ADD KEY `id_kategori` (`id_kategori`);

--
-- Indeks untuk tabel `tb_customer`
--
ALTER TABLE `tb_customer`
  ADD PRIMARY KEY (`id_customer`);

--
-- Indeks untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_jual` (`id_jual`);

--
-- Indeks untuk tabel `tb_kategori`
--
ALTER TABLE `tb_kategori`
  ADD PRIMARY KEY (`id_kategori`);

--
-- Indeks untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  ADD PRIMARY KEY (`id_jual`),
  ADD UNIQUE KEY `no_faktur` (`no_faktur`),
  ADD KEY `id_customer` (`id_customer`),
  ADD KEY `id_user` (`id_user`);

--
-- Indeks untuk tabel `tb_role`
--
ALTER TABLE `tb_role`
  ADD PRIMARY KEY (`id_role`);

--
-- Indeks untuk tabel `tb_user`
--
ALTER TABLE `tb_user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `fk_user_role` (`id_role`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT untuk tabel `tb_kategori`
--
ALTER TABLE `tb_kategori`
  MODIFY `id_kategori` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  MODIFY `id_jual` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT untuk tabel `tb_role`
--
ALTER TABLE `tb_role`
  MODIFY `id_role` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT untuk tabel `tb_user`
--
ALTER TABLE `tb_user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `tb_barang`
--
ALTER TABLE `tb_barang`
  ADD CONSTRAINT `tb_barang_ibfk_1` FOREIGN KEY (`id_kategori`) REFERENCES `tb_kategori` (`id_kategori`);

--
-- Ketidakleluasaan untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  ADD CONSTRAINT `tb_detail_penjualan_ibfk_1` FOREIGN KEY (`id_jual`) REFERENCES `tb_penjualan` (`id_jual`) ON DELETE CASCADE;

--
-- Ketidakleluasaan untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  ADD CONSTRAINT `tb_penjualan_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `tb_customer` (`id_customer`),
  ADD CONSTRAINT `tb_penjualan_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `tb_user` (`id_user`);

--
-- Ketidakleluasaan untuk tabel `tb_user`
--
ALTER TABLE `tb_user`
  ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`id_role`) REFERENCES `tb_role` (`id_role`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
