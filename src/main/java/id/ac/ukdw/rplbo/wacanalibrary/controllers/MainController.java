package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard, btnAnggota, btnKatalog, btnTransaksi, btnLaporan, btnKeluar;

    private final String IDLE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #B0BEC5; -fx-background-radius: 6; -fx-padding: 12 15;";
    private final String ACTIVE_STYLE =
            "-fx-background-color: #2E4070; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 12 15;";
    @FXML
    public void initialize() {
        // Menampilkan Dashboard sebagai halaman default saat login berhasil
        showDashboard();
    }

    private void setActiveButton(Button activeButton) {
        // Reset semua tombol ke style default
        btnDashboard.setStyle(IDLE_STYLE);
        btnAnggota.setStyle(IDLE_STYLE);
        btnKatalog.setStyle(IDLE_STYLE);
        btnTransaksi.setStyle(IDLE_STYLE);
        btnLaporan.setStyle(IDLE_STYLE);

        // Beri style aktif ke tombol yang diklik
        activeButton.setStyle(ACTIVE_STYLE);
    }

    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        loadView("/fxml/Dashboard.fxml");
    }

    @FXML
    private void showAnggota() {
        setActiveButton(btnAnggota);
        loadView("/fxml/Anggota.fxml");
    }

    @FXML
    private void showKatalog() {
        setActiveButton(btnKatalog);
        loadView("/fxml/KatalogBuku.fxml");
    }

    @FXML
    private void showTransaksi() {
        setActiveButton(btnTransaksi);
        loadView("/fxml/Transaksi.fxml"); // Hapus tanda komentar di baris ini
    }

    @FXML
    private void showLaporan() {
        setActiveButton(btnLaporan);
        loadView("/fxml/Laporan.fxml"); // Hapus tanda komentar di baris ini
    }

    @FXML
    private void handleLogout() {
        try {
            // Tutup stage utama
            Stage stage = (Stage) btnKeluar.getScene().getWindow();
            stage.close();

            // Kembali ke halaman Login
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Wacana Library - Login Sistem");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("File FXML belum dibuat: " + fxmlPath);
        }
    }
}