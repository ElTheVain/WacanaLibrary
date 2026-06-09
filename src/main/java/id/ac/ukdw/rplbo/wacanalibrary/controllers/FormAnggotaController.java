package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.models.Anggota;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class FormAnggotaController {

    @FXML private TextField txtNama;
    @FXML private ComboBox<String> cbTipe, cbStatus;

    private boolean isEditMode = false;
    private String idAnggotaEdit = "";

    @FXML
    public void initialize() {
        cbTipe.setItems(FXCollections.observableArrayList("Mahasiswa", "Dosen", "Staff"));
        cbStatus.setItems(FXCollections.observableArrayList("Aktif", "Tidak Aktif"));
        cbStatus.setValue("Aktif");
    }

    public void setEditData(Anggota anggota) {
        this.isEditMode = true;
        this.idAnggotaEdit = anggota.idAnggotaProperty().get();

        txtNama.setText(anggota.namaLengkapProperty().get());
        cbTipe.setValue(anggota.tipeProperty().get());
        cbStatus.setValue(anggota.statusProperty().get());
    }

    @FXML
    private void handleSimpan() {
        int batasPinjam = 5;
        if (cbTipe.getValue() != null && cbTipe.getValue().equals("Dosen")) batasPinjam = 10;
        else if (cbTipe.getValue() != null && cbTipe.getValue().equals("Staff")) batasPinjam = 10;

        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement pstmt;

            if (isEditMode) {
                String query = "UPDATE Anggota SET namaLengkap = ?, tipe = ?, batasPinjam = ?, status = ? WHERE idAnggota = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, txtNama.getText());
                pstmt.setString(2, cbTipe.getValue());
                pstmt.setInt(3, batasPinjam);
                pstmt.setString(4, cbStatus.getValue());
                pstmt.setString(5, idAnggotaEdit);
            } else {
                String idAnggota = "LIB-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                String tanggalSekarang = java.time.LocalDate.now().toString();

                String query = "INSERT INTO Anggota (idAnggota, namaLengkap, tipe, batasPinjam, aktifSejak, status) VALUES (?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, idAnggota);
                pstmt.setString(2, txtNama.getText());
                pstmt.setString(3, cbTipe.getValue());
                pstmt.setInt(4, batasPinjam);
                pstmt.setString(5, tanggalSekarang);
                pstmt.setString(6, cbStatus.getValue());
            }

            pstmt.executeUpdate();
            tutupJendela();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBatal() {
        tutupJendela();
    }

    private void tutupJendela() {
        Stage stage = (Stage) txtNama.getScene().getWindow();
        stage.close();
    }
}