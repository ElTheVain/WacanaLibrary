package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.dao.TransaksiDao;
import id.ac.ukdw.rplbo.wacanalibrary.dao.impl.TransaksiDaoImpl;
import id.ac.ukdw.rplbo.wacanalibrary.models.Transaksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;

public class TransaksiController {
    @FXML private TextField searchField;
    @FXML private TableView<Transaksi> tabelTransaksi;
    @FXML private TableColumn<Transaksi, String> colIdTransaksi, colAnggota, colBuku, colTglPinjam, colJatuhTempo, colStatus, colMetode;
    @FXML private TableColumn<Transaksi, Void> colOperasi;

    private ObservableList<Transaksi> transaksiList = FXCollections.observableArrayList();

    // 1. Instansiasi DAO di level class
    private final TransaksiDao transaksiDao = new TransaksiDaoImpl();

    @FXML
    public void initialize() {
        colIdTransaksi.setCellValueFactory(cellData -> cellData.getValue().idTransaksiProperty());
        colAnggota.setCellValueFactory(cellData -> cellData.getValue().idAnggotaProperty());
        colBuku.setCellValueFactory(cellData -> cellData.getValue().idBukuProperty());
        colTglPinjam.setCellValueFactory(cellData -> cellData.getValue().tanggalPinjamProperty());
        colJatuhTempo.setCellValueFactory(cellData -> cellData.getValue().tanggalJatuhTempoProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusTransaksiProperty());
        colMetode.setCellValueFactory(cellData -> cellData.getValue().metodePembayaranProperty());

        tabelTransaksi.setRowFactory(tv -> new TableRow<Transaksi>() {
            @Override
            protected void updateItem(Transaksi item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String status = item.getStatusTransaksi();
                    if (status.equalsIgnoreCase("Terlambat")) {
                        setStyle("-fx-background-color: #ffcccc;"); // Merah
                    } else if (status.equalsIgnoreCase("Selesai")) {
                        setStyle("-fx-background-color: #d4edda;"); // Hijau
                    } else if (status.equalsIgnoreCase("Berjalan")) {
                        String tglJatuhTempoStr = item.getTanggalJatuhTempo();
                        try {
                            if (tglJatuhTempoStr != null && !tglJatuhTempoStr.isEmpty() && !tglJatuhTempoStr.equals("-")) {
                                LocalDate jt = LocalDate.parse(tglJatuhTempoStr);
                                if (jt.isBefore(LocalDate.now())) {
                                    setStyle("-fx-background-color: #ffeeba;"); // Kuning
                                } else {
                                    setStyle("");
                                }
                            } else { setStyle(""); }
                        } catch (Exception e) { setStyle(""); }
                    } else { setStyle(""); }
                }
            }
        });

        // Disable visual selection highlight and clear selection
        tabelTransaksi.setStyle("-fx-selection-bar: transparent; -fx-selection-bar-text: -fx-text-background-color;");
        tabelTransaksi.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() != -1) {
                javafx.application.Platform.runLater(() -> tabelTransaksi.getSelectionModel().clearSelection());
            }
        });

        loadData();
        setupPencarianDinamis();
        setupKolomOperasi();
    }

    private void loadData() {
        // 2. Ganti blok SQL dengan pemanggilan DAO (Sangat bersih!)
        transaksiList.clear();
        transaksiList.addAll(transaksiDao.getAllTransaksi());
    }

    private void setupPencarianDinamis() {
        FilteredList<Transaksi> filteredData = new FilteredList<>(transaksiList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(trx -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                return trx.getIdTransaksi().toLowerCase().contains(filter) ||
                        trx.getIdAnggota().toLowerCase().contains(filter) ||
                        trx.getIdBuku().toLowerCase().contains(filter) ||
                        trx.getStatusTransaksi().toLowerCase().contains(filter) ||
                        trx.getMetodePembayaran().toLowerCase().contains(filter);
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
                                String status = trx.getStatusTransaksi();
                                if (status.equalsIgnoreCase("Selesai") || status.equalsIgnoreCase("Terlambat")) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(btnKembali);
                                }
                            } else { setGraphic(null); }
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
            loadData(); // Memanggil DAO lagi secara otomatis
        } catch (Exception e) { e.printStackTrace(); }
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