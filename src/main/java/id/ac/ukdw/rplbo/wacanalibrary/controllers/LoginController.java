package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    // ── LOGIN ADMIN ───────────────────────────────────────────────
    @FXML
    private void handleLoginAdmin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan Password tidak boleh kosong!");
            return;
        }
        if (validasiAdmin(username, password)) {
            bukaDashboardAdmin();
        } else {
            tampilkanError("Username atau password salah!");
        }
    }

    // ── MASUK SEBAGAI ANGGOTA (tanpa login, pilih nama) ──────────
    @FXML
    private void handleMasukAnggota() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PilihAnggota.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Pilih Akun Anggota");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);

            // Tutup login saat dialog ditutup setelah berhasil masuk
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Gagal membuka daftar anggota.");
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private boolean validasiAdmin(String username, String password) {
        String query = "SELECT 1 FROM Pegawai WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void bukaDashboardAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("Wacana Library - Dashboard Operasional");
            mainStage.setScene(new Scene(root));
            mainStage.show();
            mainStage.setMaximized(true);

            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Gagal membuka dashboard: " + e.getMessage());
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}