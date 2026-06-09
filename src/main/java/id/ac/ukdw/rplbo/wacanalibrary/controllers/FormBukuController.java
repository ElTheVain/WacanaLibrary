package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.models.Buku;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class FormBukuController {

    @FXML private TextField txtJudul, txtIsbn, txtPenulis, txtTahun, txtHalaman, txtGambar;
    @FXML private ComboBox<String> cbKategori, cbStatus;

    private boolean isEditMode = false;
    private String idBukuEdit = "";

    @FXML
    public void initialize() {
        cbKategori.setItems(FXCollections.observableArrayList(
                "Karya Umum / Referensi",
                "Ilmu Murni / Sains",
                "Teknologi / Ilmu Terapan",
                "Ilmu Sosial",
                "Bahasa",
                "Sastra",
                "Sejarah & Geografi",
                "Seni & Rekreasi"
        ));
        cbStatus.setItems(FXCollections.observableArrayList("Tersedia", "Dipinjam", "Rusak"));
        cbStatus.setValue("Tersedia");
    }

    public void setEditData(Buku buku) {
        this.isEditMode = true;
        this.idBukuEdit = buku.idBukuProperty().get();

        txtJudul.setText(buku.judulProperty().get());
        txtIsbn.setText(buku.getIsbn());
        txtPenulis.setText(buku.pengarangProperty().get());
        cbKategori.setValue(buku.kategoriProperty().get());
        txtTahun.setText(String.valueOf(buku.tahunTerbitProperty().get()));
        txtHalaman.setText(String.valueOf(buku.halamanProperty().get()));
        txtGambar.setText(buku.getGambar());
        cbStatus.setValue(buku.statusProperty().get());
    }

    @FXML
    private void handlePilihGambar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Sampul Buku");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(txtJudul.getScene().getWindow());
        if (file != null) {
            txtGambar.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSimpan() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement pstmt;

            if (isEditMode) {
                String query = "UPDATE Buku SET isbn = ?, judul = ?, pengarang = ?, kategori = ?, tahunTerbit = ?, halaman = ?, gambar = ?, status = ? WHERE idBuku = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, txtIsbn.getText());
                pstmt.setString(2, txtJudul.getText());
                pstmt.setString(3, txtPenulis.getText());
                pstmt.setString(4, cbKategori.getValue());
                pstmt.setInt(5, Integer.parseInt(txtTahun.getText()));
                pstmt.setInt(6, Integer.parseInt(txtHalaman.getText()));
                pstmt.setString(7, txtGambar.getText());
                pstmt.setString(8, cbStatus.getValue());
                pstmt.setString(9, idBukuEdit);
            } else {
                String idBuku = "B-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                String query = "INSERT INTO Buku (idBuku, isbn, judul, pengarang, tipe, kategori, tahunTerbit, halaman, gambar, status) VALUES (?, ?, ?, ?, 'Buku', ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, idBuku);
                pstmt.setString(2, txtIsbn.getText());
                pstmt.setString(3, txtJudul.getText());
                pstmt.setString(4, txtPenulis.getText());
                pstmt.setString(5, cbKategori.getValue());
                pstmt.setInt(6, Integer.parseInt(txtTahun.getText()));
                pstmt.setInt(7, Integer.parseInt(txtHalaman.getText()));
                pstmt.setString(8, txtGambar.getText());
                pstmt.setString(9, cbStatus.getValue());
            }

            pstmt.executeUpdate();
            tutupJendela();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gagal menyimpan data buku. Pastikan form diisi dengan benar.");
        }
    }

    @FXML
    private void handleBatal() {
        tutupJendela();
    }

    private void tutupJendela() {
        Stage stage = (Stage) txtJudul.getScene().getWindow();
        stage.close();
    }
}