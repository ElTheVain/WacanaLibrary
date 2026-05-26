package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;

public class Anggotariwayatcontroller {

    @FXML private ComboBox<String> filterStatus;
    @FXML private TableView<ObservableList<String>> tabelRiwayat;
    @FXML private TableColumn<ObservableList<String>, String> colIdTransaksi;
    @FXML private TableColumn<ObservableList<String>, String> colJudulBuku;
    @FXML private TableColumn<ObservableList<String>, String> colTglPinjam;
    @FXML private TableColumn<ObservableList<String>, String> colJatuhTempo;
    @FXML private TableColumn<ObservableList<String>, String> colTglKembali;
    @FXML private TableColumn<ObservableList<String>, String> colDenda;
    @FXML private TableColumn<ObservableList<String>, String> colStatusTrx;

    @FXML
    public void initialize() {
        // Setup kolom
        colIdTransaksi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colJudulBuku  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colTglPinjam  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colJatuhTempo .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colTglKembali .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colDenda      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colStatusTrx  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));

        // Isi filter dropdown
        filterStatus.setItems(FXCollections.observableArrayList(
                "Semua Status", "Dipinjam", "Selesai", "Terlambat"
        ));
        filterStatus.setValue("Semua Status");

        muatRiwayat("Semua Status");
    }

    @FXML
    private void handleFilter() {
        muatRiwayat(filterStatus.getValue());
    }

    private void muatRiwayat(String filter) {
        String idAnggota = AnggotaSession.getIdAnggota();
        if (idAnggota == null) return;

        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();

        // Query ambil semua transaksi anggota ini beserta judul buku
        String query =
                "SELECT t.idTransaksi, b.judul, t.tanggalPinjam, t.tanggalJatuhTempo, " +
                        "       t.tanggalKembali, t.totalDenda, t.statusTransaksi " +
                        "FROM Transaksi t " +
                        "JOIN Transaksi_Buku tb ON t.idTransaksi = tb.idTransaksi " +
                        "JOIN Buku b ON tb.idBuku = b.idBuku " +
                        "WHERE t.idAnggota = ? " +
                        "ORDER BY t.tanggalPinjam DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idAnggota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status  = rs.getString("statusTransaksi");
                    String kembali = rs.getString("tanggalKembali");
                    double denda   = rs.getDouble("totalDenda");

                    // Filter berdasarkan pilihan dropdown
                    if (!filter.equals("Semua Status") && !status.equalsIgnoreCase(filter)) {
                        continue;
                    }

                    ObservableList<String> row = FXCollections.observableArrayList(
                            rs.getString("idTransaksi"),
                            rs.getString("judul"),
                            rs.getString("tanggalPinjam"),
                            rs.getString("tanggalJatuhTempo") != null ? rs.getString("tanggalJatuhTempo") : "-",
                            kembali != null ? kembali : "-",
                            denda > 0 ? "Rp " + String.format("%,.0f", denda) : "-",
                            status
                    );
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabelRiwayat.setItems(rows);
    }
}