package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PilihAnggotaController {

    @FXML private ComboBox<String> cbAnggota;
    @FXML private Label            lblError;

    // Map nama -> idAnggota untuk lookup saat dipilih
    private final Map<String, String> mapAnggota = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        muatDaftarAnggota();
    }

    private void muatDaftarAnggota() {
        String query = "SELECT idAnggota, namaLengkap FROM Anggota WHERE status = 'Aktif' ORDER BY namaLengkap";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id   = rs.getString("idAnggota");
                String nama = rs.getString("namaLengkap");
                mapAnggota.put(nama, id);
            }
            cbAnggota.setItems(FXCollections.observableArrayList(mapAnggota.keySet()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMasuk() {
        String namaPilihan = cbAnggota.getValue();
        if (namaPilihan == null || namaPilihan.isEmpty()) {
            lblError.setText("Silakan pilih nama anggota terlebih dahulu.");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        // Simpan ke session
        String idPilihan = mapAnggota.get(namaPilihan);
        AnggotaSession.setAnggota(idPilihan, namaPilihan);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AnggotaMainLayout.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("Wacana Library - Portal Anggota");
            mainStage.setScene(new Scene(root));
            mainStage.show();
            mainStage.setMaximized(true);

            // Tutup dialog pilih anggota DAN jendela login
            Stage dialog = (Stage) cbAnggota.getScene().getWindow();
            dialog.close();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Gagal membuka dashboard anggota.");
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) cbAnggota.getScene().getWindow();
        stage.close();
    }
}