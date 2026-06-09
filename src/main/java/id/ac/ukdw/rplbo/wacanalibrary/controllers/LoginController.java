package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField txtUsername; // Digunakan sebagai NIM atau Username Admin
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() {
        String inputId = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (inputId.isEmpty() || password.isEmpty()) {
            tampilkanError("NIM/Identitas dan Password tidak boleh kosong!");
            return;
        }

        // 1. Validasi Admin
        if (validasiAdmin(inputId, password)) {
            bukaDashboard("/fxml/MainLayout.fxml", "Wacana Library - Dashboard Operasional");
            return;
        }

        // 2. Validasi Anggota berdasarkan NIM/NIDN/NIS
        if (validasiAnggota(inputId, password)) {
            bukaDashboard("/fxml/AnggotaMainLayout.fxml", "Wacana Library - Portal Anggota");
            return;
        }

        tampilkanError("NIM atau password salah!");
    }

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
        }
        return false;
    }

    private boolean validasiAnggota(String inputId, String password) {
        String query = "SELECT idAnggota, namaLengkap, status FROM Anggota WHERE nim = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, inputId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString("status").equalsIgnoreCase("Tidak Aktif")) {
                        tampilkanError("Akun anggota Anda sedang dinonaktifkan.");
                        return false;
                    }
                    AnggotaSession.setAnggota(rs.getString("idAnggota"), rs.getString("namaLengkap"));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void bukaDashboard(String fxml, String title) {
        try {
            Stage mainStage = new Stage();
            mainStage.setTitle(title);
            mainStage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxml))));
            mainStage.setMaximized(true);
            mainStage.show();
            ((Stage) txtUsername.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Terjadi kesalahan memuat dashboard.");
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}