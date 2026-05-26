package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.models.Transaksi;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class TransaksiController {
    @FXML private TextField searchField;
    @FXML private TableView<Transaksi> tabelTransaksi;
    @FXML private TableColumn<Transaksi, String> colIdTransaksi, colAnggota, colBuku, colTglPinjam, colJatuhTempo, colStatus, colMetode;
    @FXML private TableColumn<Transaksi, Void> colOperasi;

    private ObservableList<Transaksi> transaksiList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colIdTransaksi.setCellValueFactory(cellData -> cellData.getValue().idTransaksiProperty());
        colAnggota.setCellValueFactory(cellData -> cellData.getValue().idAnggotaProperty());
        colBuku.setCellValueFactory(cellData -> cellData.getValue().idBukuProperty());
        colTglPinjam.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().tanggalPinjamProperty().get().toString()));
        colJatuhTempo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().tanggalJatuhTempoProperty().get().toString()));
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusTransaksiProperty());
        colMetode.setCellValueFactory(cellData -> cellData.getValue().metodePembayaranProperty());

        tabelTransaksi.setRowFactory(tv -> new TableRow<Transaksi>() {
            @Override
            protected void updateItem(Transaksi item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else {
                    String status = item.statusTransaksiProperty().get();
                    if (status.equalsIgnoreCase("Terlambat")) {
                        setStyle("-fx-background-color: #ffcccc;"); // Merah
                    } else if (status.equalsIgnoreCase("Selesai")) {
                        setStyle("-fx-background-color: #d4edda;"); // Hijau
                    } else if (status.equalsIgnoreCase("Berjalan")) {
                        // Kuning jika telat tapi belum dikembalikan
                        if (item.tanggalJatuhTempoProperty().get().isBefore(LocalDate.now())) {
                            setStyle("-fx-background-color: #ffeeba;");
                        } else {
                            setStyle("");
                        }
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        loadData();
        setupPencarianDinamis();
        setupKolomOperasi();
    }

    private void loadData() {
        transaksiList.clear();
        String query = "SELECT t.idTransaksi, t.idAnggota, tb.idBuku, t.tanggalPinjam, t.tanggalJatuhTempo, t.statusTransaksi, t.metodePembayaran " +
                "FROM Transaksi t " +
                "LEFT JOIN Transaksi_Buku tb ON t.idTransaksi = tb.idTransaksi";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

//            while (rs.next()) {
//                transaksiList.add(new Transaksi(
//                        rs.getString("idTransaksi"),
//                        rs.getString("idAnggota"),
//                        rs.getString("idBuku")  == null ? "-" : rs.getString("idBuku"),
//                        LocalDate.parse(rs.getString("tanggalPinjam")),
//                        LocalDate.parse(rs.getString("tanggalJatuhTempo")),
//                        rs.getString("statusTransaksi"),
//                        rs.getString("metodePembayaran") == null ? "-" : rs.getString("metodePembayaran")
//                ));
//            }
        } catch (Exception e) { // <-- PERBAIKAN: Blok catch dan penutup yang sebelumnya hilang ditambahkan di sini
            e.printStackTrace();
        }
    }

    private void setupPencarianDinamis() {
        FilteredList<Transaksi> filteredData = new FilteredList<>(transaksiList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(trx -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                return trx.idTransaksiProperty().get().toLowerCase().contains(filter) ||
                        trx.idAnggotaProperty().get().toLowerCase().contains(filter) ||
                        trx.idBukuProperty().get().toLowerCase().contains(filter) ||
                        trx.statusTransaksiProperty().get().toLowerCase().contains(filter) ||
                        trx.metodePembayaranProperty().get().toLowerCase().contains(filter);
                        
            });
        });
        SortedList<Transaksi> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelTransaksi.comparatorProperty());
        tabelTransaksi.setItems(sortedData);
    }

    private void setupKolomOperasi() {
        Callback<TableColumn<Transaksi, Void>, TableCell<Transaksi, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Transaksi, Void> call(final TableColumn<Transaksi, Void> param) {
                return new TableCell<>() {
                    private final Button btnKembali = new Button("Kembalikan");
                    {
                        btnKembali.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                        btnKembali.setOnAction(event -> {
                            Transaksi trx = getTableView().getItems().get(getIndex());
                            prosesPengembalianBuku(trx);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Transaksi trx = getTableView().getItems().get(getIndex());
                            if (trx != null) {
                                String status = trx.statusTransaksiProperty().get();

                                if (status.equalsIgnoreCase("Selesai") || status.equalsIgnoreCase("Terlambat")) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(btnKembali);
                                }
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
        colOperasi.setCellFactory(cellFactory);
    }

    @FXML
    private void bukaFormPeminjaman() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormTransaksi.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Proses Peminjaman Baru");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prosesPengembalianBuku(Transaksi trx) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormPengembalian.fxml"));
            Parent root = loader.load();

            FormPengembalianController controller = loader.getController();
            controller.setTransaksiData(trx);

            Stage stage = new Stage();
            stage.setTitle("Proses Pengembalian & Denda");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gagal memuat FormPengembalian.fxml");
        }
    }
}