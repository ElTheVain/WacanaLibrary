package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.*;

public class AnggotaKatalogController {

    @FXML private TextField  searchField;
    @FXML private TableView<ObservableList<String>> tabelBuku;
    @FXML private TableColumn<ObservableList<String>, String> colId;
    @FXML private TableColumn<ObservableList<String>, String> colJudul;
    @FXML private TableColumn<ObservableList<String>, String> colPengarang;
    @FXML private TableColumn<ObservableList<String>, String> colKategori;
    @FXML private TableColumn<ObservableList<String>, String> colTahun;
    @FXML private TableColumn<ObservableList<String>, String> colHalaman;
    @FXML private TableColumn<ObservableList<String>, String> colStatus;

    @FXML
    public void initialize() {
        colId        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colJudul     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colPengarang .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colKategori  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colTahun     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colHalaman   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colStatus    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));

        muatBuku("");
    }

    @FXML
    private void handleCari() {
        muatBuku(searchField.getText().trim());
    }

    private void muatBuku(String keyword) {
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        String query = "SELECT idBuku, judul, pengarang, kategori, tahunTerbit, halaman, status " +
                "FROM Buku WHERE judul LIKE ? OR pengarang LIKE ? OR kategori LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList(
                            rs.getString("idBuku"),
                            rs.getString("judul"),
                            rs.getString("pengarang"),
                            rs.getString("kategori"),
                            String.valueOf(rs.getInt("tahunTerbit")),
                            String.valueOf(rs.getInt("halaman")),
                            rs.getString("status")
                    );
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tabelBuku.setItems(rows);
    }
}