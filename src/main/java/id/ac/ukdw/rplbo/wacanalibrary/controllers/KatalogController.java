package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.models.Buku;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.sql.*;

public class KatalogController {

    @FXML private TextField searchField;
    @FXML private TableView<Buku> tabelBuku;

    // TAMBAHAN: Kolom Gambar
    @FXML private TableColumn<Buku, String> colGambar;
    @FXML private TableColumn<Buku, Void> colOperasi;
    @FXML private TableColumn<Buku, String> colId, colIsbn, colJudul, colPengarang, colKategori, colStatus;
    @FXML private TableColumn<Buku, Number> colTahun, colHalaman;

    private ObservableList<Buku> bukuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idBukuProperty());
        if(colIsbn != null) colIsbn.setCellValueFactory(cellData -> cellData.getValue().isbnProperty());
        colJudul.setCellValueFactory(cellData -> cellData.getValue().judulProperty());
        colPengarang.setCellValueFactory(cellData -> cellData.getValue().pengarangProperty());
        colKategori.setCellValueFactory(cellData -> cellData.getValue().kategoriProperty());
        colTahun.setCellValueFactory(cellData -> cellData.getValue().tahunTerbitProperty());
        if(colHalaman != null) colHalaman.setCellValueFactory(cellData -> cellData.getValue().halamanProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // RENDER GAMBAR KE DALAM TABEL
        if(colGambar != null) {
            colGambar.setCellValueFactory(cellData -> cellData.getValue().gambarProperty());
            colGambar.setCellFactory(column -> new TableCell<Buku, String>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(60);
                    // Agar gambar rapi di dalam sel tanpa gepeng
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String path, boolean empty) {
                    super.updateItem(path, empty);
                    if (empty || path == null || path.trim().isEmpty()) {
                        setGraphic(null);
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            imageView.setImage(new Image(file.toURI().toString()));
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }

        // Peringatan Warna Status
        tabelBuku.setRowFactory(tv -> new TableRow<Buku>() {
            @Override
            protected void updateItem(Buku item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getStatus().equalsIgnoreCase("Rusak")) setStyle("-fx-background-color: #ffcccc;");
                else if (item.getStatus().equalsIgnoreCase("Dipinjam")) setStyle("-fx-background-color: #fff3cd;");
                else setStyle("-fx-background-color: #d4edda;");

            }
        });

        // Disable visual selection highlight and clear selection
        tabelBuku.setStyle("-fx-selection-bar: transparent; -fx-selection-bar-text: -fx-text-background-color;");
        tabelBuku.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() != -1) {
                javafx.application.Platform.runLater(() -> tabelBuku.getSelectionModel().clearSelection());
            }
        });

        loadDataFromDatabase();
        setupPencarianDinamis();
        setupKolomOperasi();
    }

    private void loadDataFromDatabase() {
        bukuList.clear();
        String query = "SELECT * FROM Buku";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bukuList.add(new Buku(
                        rs.getString("idBuku"), rs.getString("isbn"), rs.getString("judul"),
                        rs.getString("pengarang"), rs.getString("tipe"), rs.getString("kategori"),
                        rs.getInt("tahunTerbit"), rs.getInt("halaman"), rs.getString("gambar"), rs.getString("status")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupPencarianDinamis() {
        FilteredList<Buku> filteredData = new FilteredList<>(bukuList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(buku -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return buku.judulProperty().get().toLowerCase().contains(lower) ||
                        (buku.getIsbn() != null && buku.getIsbn().toLowerCase().contains(lower)) ||
                        buku.pengarangProperty().get().toLowerCase().contains(lower) ||
                        buku.idBukuProperty().get().toLowerCase().contains(lower);
            });
        });
        SortedList<Buku> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelBuku.comparatorProperty());
        tabelBuku.setItems(sortedData);
    }

    @FXML private void bukaFormTambah() { bukaForm(null); }

    private void setupKolomOperasi() {
        colOperasi.setCellFactory(param -> new TableCell<Buku, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnHapus = new Button("Hapus");
            private final HBox pane = new HBox(5, btnEdit, btnHapus);
            {
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                btnEdit.setOnAction(e -> bukaForm(getTableView().getItems().get(getIndex())));
                btnHapus.setOnAction(e -> hapusDataBuku(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void bukaForm(Buku bukuEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormBuku.fxml"));
            Stage stage = new Stage();
            stage.setTitle(bukuEdit == null ? "Tambah Buku Baru" : "Ubah Data Buku");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            if (bukuEdit != null) {
                FormBukuController controller = loader.getController();
                controller.setEditData(bukuEdit);
            }

            stage.showAndWait();
            loadDataFromDatabase();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hapusDataBuku(Buku buku) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus Buku: " + buku.judulProperty().get() + "?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Buku WHERE idBuku = ?")) {
                    pstmt.setString(1, buku.idBukuProperty().get());
                    pstmt.executeUpdate();
                    loadDataFromDatabase();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Buku gagal dihapus (terkait data transaksi).").show();
                }
            }
        });
    }
}