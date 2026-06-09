package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.UUID;

public class FormTransaksiController {

    @FXML private ComboBox<String> cbAnggota, cbBuku;
    @FXML private DatePicker dpTglPinjam;
    @FXML private ListView<String> listKeranjang;

    private ObservableList<String> dataKeranjang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadDataComboBox();
        dpTglPinjam.setValue(LocalDate.now());
        listKeranjang.setItems(dataKeranjang);

        listKeranjang.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText((getIndex() + 1) + ". " + item);
                }
            }
        });
    }

    private void loadDataComboBox() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstAnggota = conn.prepareStatement("SELECT idAnggota, namaLengkap FROM Anggota WHERE status = 'Aktif'");
             PreparedStatement pstBuku = conn.prepareStatement("SELECT idBuku, judul FROM Buku WHERE status = 'Tersedia'")) {

            ResultSet rsAnggota = pstAnggota.executeQuery();
            while (rsAnggota.next()) cbAnggota.getItems().add(rsAnggota.getString("idAnggota") + " - " + rsAnggota.getString("namaLengkap"));

            ResultSet rsBuku = pstBuku.executeQuery();
            while (rsBuku.next()) cbBuku.getItems().add(rsBuku.getString("idBuku") + " - " + rsBuku.getString("judul"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambahKeKeranjang() {
        if (cbAnggota.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Silakan pilih Anggota terlebih dahulu untuk mengecek kuota peminjaman!").show();
            return;
        }

        String bukuTerpilih = cbBuku.getValue();
        if (bukuTerpilih == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih buku terlebih dahulu dari daftar!").show();
            return;
        }

        if (dataKeranjang.contains(bukuTerpilih)) {
            new Alert(Alert.AlertType.WARNING, "Buku ini sudah ada di keranjang!").show();
            return;
        }

        // CEK KUOTA PINJAM (Validasi Fail-Fast)
        String idAnggota = cbAnggota.getValue().split(" - ")[0];
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstCekKuota = conn.prepareStatement("SELECT batasPinjam FROM Anggota WHERE idAnggota = ?")) {

            pstCekKuota.setString(1, idAnggota);
            ResultSet rs = pstCekKuota.executeQuery();
            if (rs.next()) {
                int batasPinjam = rs.getInt("batasPinjam");

                // Cek apakah jumlah buku di keranjang sudah mencapai atau melebihi batas
                if (dataKeranjang.size() >= batasPinjam) {
                    new Alert(Alert.AlertType.ERROR, "Sisa kuota peminjaman anggota ini maksimal hanya " + batasPinjam + " buku.").showAndWait();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataKeranjang.add(bukuTerpilih);
        cbBuku.setValue(null);
    }

    @FXML
    private void handleHapusDariKeranjang() {
        String terpilih = listKeranjang.getSelectionModel().getSelectedItem();
        if (terpilih != null) {
            dataKeranjang.remove(terpilih);
        }
    }

    @FXML
    private void handleSimpan() {
        if (cbAnggota.getValue() == null || dataKeranjang.isEmpty() || dpTglPinjam.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih anggota, masukkan minimal 1 buku ke keranjang, dan pastikan tanggal terisi!").show();
            return;
        }

        String idAnggota = cbAnggota.getValue().split(" - ")[0];
        LocalDate tglPinjam = dpTglPinjam.getValue();
        int jumlahBukuDiKeranjang = dataKeranjang.size();

        try (Connection conn = DatabaseHelper.getConnection()) {

            conn.setAutoCommit(false);

            // ID Transaksi Induk
            String idTransaksi = "TRX-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            int maxDurasi = 3;

            for (String itemBuku : dataKeranjang) {

                String idBuku = itemBuku.split(" - ")[0];

                // Cek ketebalan buku
                PreparedStatement pstCekBuku = conn.prepareStatement("SELECT halaman FROM Buku WHERE idBuku = ?");
                pstCekBuku.setString(1, idBuku);

                ResultSet rsCek = pstCekBuku.executeQuery();

                if (rsCek.next()) {
                    int halaman = rsCek.getInt("halaman");
                    int durasiBukuIni;

                    if (halaman <= 100) {
                        durasiBukuIni = 3;
                    } else if (halaman <= 250) {
                        durasiBukuIni = 5;
                    } else {
                        durasiBukuIni = 7;
                    }

                    // Ambil durasi terlama
                    if (durasiBukuIni > maxDurasi) {
                        maxDurasi = durasiBukuIni;
                    }
                }

                // Catat ke Tabel Pivot (Relasi)
                PreparedStatement pstPivot = conn.prepareStatement("INSERT INTO Transaksi_Buku (idTransaksi, idBuku) VALUES (?, ?)");
                pstPivot.setString(1, idTransaksi);
                pstPivot.setString(2, idBuku);
                pstPivot.executeUpdate();

                // Update Status Buku menjadi Dipinjam
                PreparedStatement pstBuku = conn.prepareStatement("UPDATE Buku SET status = 'Dipinjam' WHERE idBuku = ?");
                pstBuku.setString(1, idBuku);
                pstBuku.executeUpdate();
            }

            // 3. Catat Transaksi
            LocalDate jatuhTempo = tglPinjam.plusDays(maxDurasi);
            String sqlTrx = "INSERT INTO Transaksi (idTransaksi, idAnggota, idPegawai, tanggalPinjam, tanggalJatuhTempo, statusTransaksi, totalDenda, metodePembayaran) VALUES (?, ?, 'PEG-001', ?, ?, 'Berjalan', 0, '-')";
            PreparedStatement pstTrx = conn.prepareStatement(sqlTrx);
            pstTrx.setString(1, idTransaksi);
            pstTrx.setString(2, idAnggota);
            pstTrx.setString(3, tglPinjam.toString());
            pstTrx.setString(4, jatuhTempo.toString());
            pstTrx.executeUpdate();

            // 4. Kurangi Kuota Anggota Sesuai Jumlah Keranjang
            PreparedStatement pstUpdateAnggota = conn.prepareStatement("UPDATE Anggota SET batasPinjam = batasPinjam - ? WHERE idAnggota = ?");
            pstUpdateAnggota.setInt(1, jumlahBukuDiKeranjang);
            pstUpdateAnggota.setString(2, idAnggota);
            pstUpdateAnggota.executeUpdate();

            conn.commit();
            tutupJendela();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memproses transaksi: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleBatal() {
        tutupJendela();
    }

    private void tutupJendela() {
        Stage stage = (Stage) cbAnggota.getScene().getWindow();
        stage.close();
    }
}