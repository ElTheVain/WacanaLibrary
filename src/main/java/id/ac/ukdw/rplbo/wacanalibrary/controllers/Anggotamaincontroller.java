package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Anggotamaincontroller {

    @FXML private StackPane contentArea;
    @FXML private Label     lblNamaAnggota;
    @FXML private Button    btnDashboard;
    @FXML private Button    btnKatalog;
    @FXML private Button    btnRiwayat;

    private static final String STYLE_AKTIF =
            "-fx-background-color: #2E4070; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 12 15;";
    private static final String STYLE_NONAKTIF =
            "-fx-background-color: transparent; -fx-text-fill: #B0BEC5; -fx-background-radius: 6; -fx-padding: 12 15;";

    @FXML
    public void initialize() {
        // Tampilkan nama anggota dari session
        String nama = AnggotaSession.getNamaAnggota();
        if (nama != null && !nama.isEmpty()) {
            lblNamaAnggota.setText("Halo, " + nama + "!");
        }
        // Default tampilkan dashboard
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        setAktif(btnDashboard);
        muatHalaman("/fxml/AnggotaDashboard.fxml");
    }

    @FXML
    private void showKatalog() {
        setAktif(btnKatalog);
        muatHalaman("/fxml/AnggotaKatalog.fxml");
    }

    @FXML
    private void showRiwayat() {
        setAktif(btnRiwayat);
        muatHalaman("/fxml/AnggotaRiwayat.fxml");
    }

    @FXML
    private void handleKeluar() {
        AnggotaSession.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Wacana Library - Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Tutup jendela anggota
            Stage current = (Stage) contentArea.getScene().getWindow();
            current.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void muatHalaman(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Ubah style tombol sidebar: aktif vs nonaktif */
    private void setAktif(Button aktif) {
        btnDashboard.setStyle(STYLE_NONAKTIF);
        btnKatalog.setStyle(STYLE_NONAKTIF);
        btnRiwayat.setStyle(STYLE_NONAKTIF);
        aktif.setStyle(STYLE_AKTIF);
    }
}