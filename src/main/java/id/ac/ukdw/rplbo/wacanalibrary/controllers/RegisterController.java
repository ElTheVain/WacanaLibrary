package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class RegisterController {

    @FXML private TextField  txtNama;
    @FXML private TextField  txtIdAnggota;
    @FXML private ComboBox<String> cbTipe;
    @FXML private Label      lblError;
    @FXML private Label      lblSuccess;

    @FXML
    public void initialize() {
        cbTipe.getItems().addAll("Mahasiswa", "Dosen", "Staff", "Umum");
        cbTipe.getSelectionModel().selectFirst();
        txtIdAnggota.setText(generateId());
    }

    private String generateId() {
        Random rnd = new Random();
        return String.format("LIB-%04X", rnd.nextInt(0xFFFF));
    }

    @FXML
    private void handleDaftar() {
        String nama  = txtNama.getText().trim();
        String id    = txtIdAnggota.getText().trim();
        String tipe  = cbTipe.getValue();

        sembunyikanPesan();

        if (nama.isEmpty()) {
            tampilkanError("Nama lengkap tidak boleh kosong!");
            return;
        }
        if (tipe == null) {
            tampilkanError("Pilih tipe anggota terlebih dahulu.");
            return;
        }

        int batasPinjam = switch (tipe) {
            case "Dosen"  -> 10;
            case "Staff"  -> 7;
            case "Umum"   -> 3;
            default       -> 5; // Mahasiswa
        };

        String tanggal = LocalDate.now().toString();

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Anggota (idAnggota, namaLengkap, tipe, batasPinjam, aktifSejak, status) " +
                             "VALUES (?, ?, ?, ?, ?, 'Aktif')")) {

            ps.setString(1, id);
            ps.setString(2, nama);
            ps.setString(3, tipe);
            ps.setInt(4, batasPinjam);
            ps.setString(5, tanggal);
            ps.executeUpdate();

            tampilkanSukses("✅ Pendaftaran berhasil! ID Anggota Anda: " + id +
                    ". Silakan login menggunakan akun anggota.");
            txtNama.clear();
            txtIdAnggota.setText(generateId());

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                tampilkanError("ID sudah digunakan. Coba lagi.");
                txtIdAnggota.setText(generateId());
            } else {
                tampilkanError("Gagal mendaftar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleKeLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Wacana Library — Masuk");
            loginStage.setScene(new Scene(root));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setResizable(false);

            // Tutup window register
            Stage thisStage = (Stage) txtNama.getScene().getWindow();
            thisStage.close();

            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
        lblError.setManaged(true);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);
    }

    private void tampilkanSukses(String pesan) {
        lblSuccess.setText(pesan);
        lblSuccess.setVisible(true);
        lblSuccess.setManaged(true);
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void sembunyikanPesan() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);
    }
}
