package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.models.Anggota;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
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

public class AnggotaController {
    @FXML private TextField searchField;
    @FXML private TableView<Anggota> tabelAnggota;

    @FXML private TableColumn<Anggota, Void> colOperasi;
    @FXML private TableColumn<Anggota, String> colId, colNim, colNama, colTipe, colStatus;
    @FXML private TableColumn<Anggota, Number> colBatas;

    private ObservableList<Anggota> anggotaList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idAnggotaProperty());

        if (colNim != null) {
            colNim.setCellValueFactory(cellData -> cellData.getValue().nimProperty());
        }

        colNama.setCellValueFactory(cellData -> cellData.getValue().namaLengkapProperty());
        colTipe.setCellValueFactory(cellData -> cellData.getValue().tipeProperty());
        colBatas.setCellValueFactory(cellData -> cellData.getValue().batasPinjamProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tabelAnggota.setRowFactory(tv -> new TableRow<Anggota>() {
            @Override
            protected void updateItem(Anggota item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.statusProperty().get().equalsIgnoreCase("Tidak Aktif")) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else setStyle("");
            }
        });

        // Disable visual selection highlight and clear selection
        tabelAnggota.setStyle("-fx-selection-bar: transparent; -fx-selection-bar-text: -fx-text-background-color;");
        tabelAnggota.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() != -1) {
                javafx.application.Platform.runLater(() -> tabelAnggota.getSelectionModel().clearSelection());
            }
        });

        loadData();
        setupPencarianDinamis();
        setupKolomOperasi();
    }

    private void loadData() {
        anggotaList.clear();
        String query = "SELECT * FROM Anggota";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // Pemanggilan constructor disesuaikan menjadi 7 argumen (tanpa username)
                anggotaList.add(new Anggota(
                        rs.getString("idAnggota"),
                        rs.getString("nim"),
                        rs.getString("namaLengkap"),
                        rs.getString("tipe"),
                        rs.getInt("batasPinjam"),
                        rs.getString("aktifSejak"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPencarianDinamis() {
        FilteredList<Anggota> filteredData = new FilteredList<>(anggotaList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(anggota -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                if (anggota.namaLengkapProperty().get().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (anggota.nimProperty().get() != null && anggota.nimProperty().get().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (anggota.idAnggotaProperty().get().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (anggota.tipeProperty().get().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (String.valueOf(anggota.batasPinjamProperty().get()).contains(lowerCaseFilter)) return true;
                else if (anggota.statusProperty().get().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<Anggota> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelAnggota.comparatorProperty());
        tabelAnggota.setItems(sortedData);
    }

    @FXML
    private void bukaFormTambahAnggota() {
        bukaForm(null);
    }

    private void setupKolomOperasi() {
        Callback<TableColumn<Anggota, Void>, TableCell<Anggota, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Anggota, Void> call(final TableColumn<Anggota, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnHapus = new Button("Hapus");
                    private final HBox pane = new HBox(5, btnEdit, btnHapus);

                    {
                        btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");

                        btnEdit.setOnAction(event -> {
                            Anggota data = getTableView().getItems().get(getIndex());
                            bukaForm(data);
                        });

                        btnHapus.setOnAction(event -> {
                            Anggota data = getTableView().getItems().get(getIndex());
                            hapusDataAnggota(data);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else setGraphic(pane);
                    }
                };
            }
        };
        colOperasi.setCellFactory(cellFactory);
    }

    private void bukaForm(Anggota anggotaEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormAnggota.fxml"));
            Parent root = loader.load();

            FormAnggotaController controller = loader.getController();

            if (anggotaEdit != null) {
                controller.setEditData(anggotaEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(anggotaEdit == null ? "Tambah Anggota Baru" : "Edit Data Anggota");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hapusDataAnggota(Anggota anggota) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Anggota: " + anggota.namaLengkapProperty().get() + "?");
        alert.setContentText("Data yang dihapus tidak dapat dikembalikan.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Anggota WHERE idAnggota = ?")) {
                    pstmt.setString(1, anggota.idAnggotaProperty().get());
                    pstmt.executeUpdate();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}