package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AnggotaDashboardController {

    @FXML private Label lblSalam;
    @FXML private Label lblTanggal;
    @FXML private Label lblJumlahPinjam;
    @FXML private Label lblStatusTerlambat;
    @FXML private Label lblTotalDenda;

    @FXML private TableView<ObservableList<String>> tabelPinjamanAktif;
    @FXML private TableColumn<ObservableList<String>, String> colIdTransaksi;
    @FXML private TableColumn<ObservableList<String>, String> colJudulBuku;
    @FXML private TableColumn<ObservableList<String>, String> colTglPinjam;
    @FXML private TableColumn<ObservableList<String>, String> colJatuhTempo;
    @FXML private TableColumn<ObservableList<String>, String> colStatusPinjam;

    @FXML
    public void initialize() {
        String nama = AnggotaSession.getNamaAnggota();
        String id   = AnggotaSession.getIdAnggota();

        // Salam & tanggal
        lblSalam.setText("Selamat Datang, " + (nama != null ? nama : "Anggota") + "!");
        lblTanggal.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
                        new java.util.Locale("id", "ID"))));

        // Setup kolom tabel
        colIdTransaksi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colJudulBuku  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colTglPinjam  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colJatuhTempo .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colStatusPinjam.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));

        if (id != null) muatData(id);
    }

    private void muatData(String idAnggota) {
        int jumlahPinjam   = 0;
        int jumlahTerlambat = 0;
        double totalDenda  = 0;
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();

        // Query: ambil semua transaksi aktif anggota ini beserta buku-bukunya
        String query =
                "SELECT t.idTransaksi, b.judul, t.tanggalPinjam, t.tanggalJatuhTempo, " +
                        "       t.totalDenda, t.statusTransaksi " +
                        "FROM Transaksi t " +
                        "JOIN Transaksi_Buku tb ON t.idTransaksi = tb.idTransaksi " +
                        "JOIN Buku b ON tb.idBuku = b.idBuku " +
                        "WHERE t.idAnggota = ? AND t.statusTransaksi != 'Selesai'";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idAnggota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jumlahPinjam++;
                    String jatuhTempo = rs.getString("tanggalJatuhTempo");
                    double denda      = rs.getDouble("totalDenda");
                    totalDenda += denda;

                    // Cek terlambat
                    boolean terlambat = false;
                    if (jatuhTempo != null) {
                        LocalDate tgl = LocalDate.parse(jatuhTempo);
                        terlambat = LocalDate.now().isAfter(tgl);
                        if (terlambat) jumlahTerlambat++;
                    }

                    String statusTampil = terlambat ? "⚠ Terlambat" : "✔ Tepat Waktu";

                    ObservableList<String> row = FXCollections.observableArrayList(
                            rs.getString("idTransaksi"),
                            rs.getString("judul"),
                            rs.getString("tanggalPinjam"),
                            jatuhTempo != null ? jatuhTempo : "-",
                            statusTampil
                    );
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update label
        lblJumlahPinjam.setText(String.valueOf(jumlahPinjam));
        lblStatusTerlambat.setText(String.valueOf(jumlahTerlambat));
        lblTotalDenda.setText("Rp " + String.format("%,.0f", totalDenda));
        tabelPinjamanAktif.setItems(rows);
    }
}