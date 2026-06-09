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

    @FXML private TextField txtNama, txtNim, txtIdAnggota, txtPasswordVisible;
    @FXML private PasswordField txtPasswordHidden;
    @FXML private Button btnTogglePass;
    @FXML private ComboBox<String> cbTipe;
    @FXML private Label lblError, lblSuccess;

    @FXML
    public void initialize() {
        cbTipe.getItems().addAll("Mahasiswa", "Dosen", "Staff", "Umum");
        cbTipe.getSelectionModel().selectFirst();
        txtIdAnggota.setText(generateId());

        // Sinkronisasi input dan batasan maksimum 8 karakter
        txtPasswordHidden.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 8) {
                txtPasswordHidden.setText(oldVal);
            } else if (!txtPasswordVisible.getText().equals(newVal)) {
                txtPasswordVisible.setText(newVal);
            }
        });

        txtPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 8) {
                txtPasswordVisible.setText(oldVal);
            } else if (!txtPasswordHidden.getText().equals(newVal)) {
                txtPasswordHidden.setText(newVal);
            }
        });
    }

    @FXML
    private void togglePassword() {
        if (txtPasswordHidden.isVisible()) {
            txtPasswordHidden.setVisible(false);
            txtPasswordVisible.setVisible(true);
            btnTogglePass.setText("Sembunyikan");
        } else {
            txtPasswordHidden.setVisible(true);
            txtPasswordVisible.setVisible(false);
            btnTogglePass.setText("Lihat");
        }
    }

    private String generateId() {
        return String.format("LIB-%04X", new Random().nextInt(0xFFFF));
    }

    @FXML
    private void handleDaftar() {
        String nama = txtNama.getText().trim();
        String nim = txtNim.getText().trim();
        String pass = txtPasswordHidden.isVisible() ? txtPasswordHidden.getText() : txtPasswordVisible.getText();
        String id = txtIdAnggota.getText().trim();
        String tipe = cbTipe.getValue();

        sembunyikanPesan();

        if (nama.isEmpty() || nim.isEmpty() || pass.isEmpty()) {
            tampilkanError("Semua kolom (Nama, NIM/NIS, Password) wajib diisi!");
            return;
        }

        int batasPinjam = switch (tipe) { case "Dosen" -> 10; case "Staff" -> 7; case "Umum" -> 3; default -> 5; };
        String tanggal = LocalDate.now().toString();

        String query = "INSERT INTO Anggota (idAnggota, nim, namaLengkap, password, tipe, batasPinjam, aktifSejak, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Aktif')";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id);
            ps.setString(2, nim);
            ps.setString(3, nama);
            ps.setString(4, pass);
            ps.setString(5, tipe);
            ps.setInt(6, batasPinjam);
            ps.setString(7, tanggal);
            ps.executeUpdate();

            tampilkanSukses("Pendaftaran berhasil! Silakan login menggunakan NIM.");
            txtNama.clear(); txtNim.clear();
            txtPasswordHidden.clear(); txtPasswordVisible.clear();
            txtIdAnggota.setText(generateId());

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                tampilkanError("NIM/NIS atau ID sudah terdaftar.");
            } else {
                tampilkanError("Gagal mendaftar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleKeLogin() {
        try {
            Stage loginStage = new Stage();
            loginStage.setTitle("Wacana Library — Masuk");
            loginStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"))));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setResizable(false);
            ((Stage) txtNama.getScene().getWindow()).close();
            loginStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void tampilkanError(String pesan) { lblError.setText(pesan); lblError.setVisible(true); lblError.setManaged(true); lblSuccess.setVisible(false); lblSuccess.setManaged(false); }
    private void tampilkanSukses(String pesan) { lblSuccess.setText(pesan); lblSuccess.setVisible(true); lblSuccess.setManaged(true); lblError.setVisible(false); lblError.setManaged(false); }
    private void sembunyikanPesan() { lblError.setVisible(false); lblError.setManaged(false); lblSuccess.setVisible(false); lblSuccess.setManaged(false); }
}