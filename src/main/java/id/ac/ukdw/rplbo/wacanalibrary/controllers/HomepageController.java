package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomepageController {

    @FXML private TextField searchField;
    @FXML private FlowPane  bookGrid;
    @FXML private VBox      emptyState;

    @FXML private Label lblJumlahHasil;
    @FXML private Button chipSemua, chipSastra, chipSains, chipTeknologi, chipSejarah, chipBahasa;

    private String activeKategori = "";

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            muatBuku(newVal.trim(), activeKategori);
        });

        muatBuku("", "");

        javafx.application.Platform.runLater(() -> {
            try {
                bookGrid.getScene().getWindow().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        muatBuku(searchField.getText().trim(), activeKategori);
                    }
                });

                bookGrid.getScene().getRoot().setOnMouseEntered(e -> {
                    muatBuku(searchField.getText().trim(), activeKategori);
                });
            } catch (Exception e) {
            }
        });
    }

    @FXML
    private void handleCari() { muatBuku(searchField.getText().trim(), activeKategori); }

    @FXML
    private void handleCariButton() {
        searchField.clear();
        activeKategori = "";
        setActiveChip(chipSemua);
        muatBuku("", "");
        searchField.requestFocus();
    }

    @FXML
    private void filterSemua() {
        activeKategori = "";
        setActiveChip(chipSemua);
        muatBuku(searchField.getText().trim(), "");
    }

    @FXML
    private void filterKategori(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        activeKategori = clicked.getText();
        setActiveChip(clicked);
        muatBuku(searchField.getText().trim(), activeKategori);
    }

    private void setActiveChip(Button active) {
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #64748B; -fx-border-color: #CBD5E1; -fx-border-radius: 999; -fx-background-radius: 999; -fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: none;";
        String activeStyle = "-fx-background-color: #185FA5; -fx-text-fill: white; -fx-background-radius: 999; -fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: none;";

        List<Button> chips = List.of(chipSemua, chipSastra, chipSains, chipTeknologi, chipSejarah, chipBahasa);
        for (Button chip : chips) {
            chip.setStyle(chip == active ? activeStyle : inactiveStyle);
        }
    }

    private void muatBuku(String keyword, String kategori) {
        bookGrid.getChildren().clear();
        List<String[]> bukuList = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT idBuku, isbn, judul, pengarang, kategori, tahunTerbit, halaman, gambar, status FROM Buku WHERE 1=1");

        if (!keyword.isEmpty()) {
            query.append(" AND (judul LIKE ? OR pengarang LIKE ? OR kategori LIKE ? OR isbn LIKE ?)");
        }
        if (!kategori.isEmpty()) {
            query.append(" AND kategori LIKE ?");
        }
        query.append(" ORDER BY judul ASC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            int idx = 1;
            if (!keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (!kategori.isEmpty()) {
                ps.setString(idx, "%" + kategori + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bukuList.add(new String[]{
                            rs.getString("idBuku"),      // 0
                            rs.getString("isbn"),        // 1
                            rs.getString("judul"),       // 2
                            rs.getString("pengarang"),   // 3
                            rs.getString("kategori"),    // 4
                            String.valueOf(rs.getInt("tahunTerbit")), // 5
                            String.valueOf(rs.getInt("halaman")),     // 6
                            rs.getString("gambar"),      // 7
                            rs.getString("status")       // 8
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (bukuList.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            lblJumlahHasil.setText("0 buku ditemukan");
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            lblJumlahHasil.setText(bukuList.size() + " buku ditemukan");
            for (String[] buku : bukuList) {
                bookGrid.getChildren().add(buatKartuBuku(buku));
            }
        }
    }

    private VBox buatKartuBuku(String[] buku) {
        String isbn        = buku[1];
        String judul       = buku[2];
        String pengarang   = buku[3];
        String kategori    = buku[4];
        String tahun       = buku[5];
        String gambarPath  = buku[7];
        String dbStatus    = buku[8];

        boolean tersedia = "Tersedia".equalsIgnoreCase(dbStatus);
        String labelStatusTampil = tersedia ? "Tersedia" : "Tidak Tersedia";

        VBox card = new VBox(0);
        card.setPrefWidth(175);
        card.setMaxWidth(175);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: none; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        StackPane cover = new StackPane();
        cover.setPrefHeight(250);
        cover.setMinHeight(250);
        cover.setStyle("-fx-background-color: " + coverColor(kategori) + "; -fx-background-radius: 10 10 0 0;");

        if (gambarPath != null && !gambarPath.trim().isEmpty()) {
            File imgFile = new File(gambarPath);
            if (imgFile.exists()) {
                Image img = new Image(imgFile.toURI().toString());

                Region imgRegion = new Region();
                imgRegion.setPrefSize(175, 250);

                BackgroundSize bgSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
                BackgroundImage bgImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSize);
                imgRegion.setBackground(new Background(bgImg));

                Rectangle clip = new Rectangle(175, 250);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imgRegion.setClip(clip);

                cover.getChildren().add(imgRegion);
            }
        }

        Label badge = new Label(labelStatusTampil);
        badge.setStyle("-fx-background-color: " + (tersedia ? "#DCFCE7" : "#FEE2E2") + ";" +
                "-fx-text-fill: " + (tersedia ? "#15803D" : "#B91C1C") + ";" +
                "-fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 2 8;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(8, 8, 0, 0));
        cover.getChildren().add(badge);

        VBox info = new VBox(4);
        info.setPadding(new Insets(10, 12, 12, 12));

        Label lblJudul = new Label(judul);
        lblJudul.setWrapText(true);
        lblJudul.setMaxHeight(38);
        lblJudul.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1B2A4A; -fx-wrap-text: true;");

        Label lblPengarang = new Label(pengarang);
        lblPengarang.setStyle("-fx-font-size: 11; -fx-text-fill: #64748B;");
        lblPengarang.setWrapText(true);
        lblPengarang.setMaxHeight(28);

        Label lblIsbn = new Label("ISBN: " + (isbn != null && !isbn.isEmpty() ? isbn : "-"));
        lblIsbn.setStyle("-fx-font-size: 10; -fx-text-fill: #94A3B8;");

        HBox bottom = new HBox(6);
        bottom.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(bottom, new Insets(4, 0, 0, 0));

        Label lblKat = new Label(singkatKategori(kategori));
        lblKat.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-size: 10; -fx-background-radius: 999; -fx-padding: 2 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTahun = new Label(tahun);
        lblTahun.setStyle("-fx-font-size: 10; -fx-text-fill: #94A3B8;");

        bottom.getChildren().addAll(lblKat, spacer, lblTahun);

        info.getChildren().addAll(lblJudul, lblPengarang, lblIsbn, bottom);
        card.getChildren().addAll(cover, info);

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("-fx-border-color: #E2E8F0", "-fx-border-color: #93C5FD")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-border-color: #93C5FD", "-fx-border-color: #E2E8F0")));

        return card;
    }

    private String coverColor(String kategori) {
        if (kategori == null) return "#F1F5F9";
        return switch (kategori) {
            case "Sastra"                        -> "#FFF7ED";
            case "Ilmu Murni / Sains"            -> "#F0FDF4";
            case "Teknologi / Ilmu Terapan"      -> "#EFF6FF";
            case "Sejarah & Geografi"            -> "#FFFBEB";
            case "Bahasa"                        -> "#FDF4FF";
            case "Ilmu Sosial"                   -> "#FFF1F2";
            case "Karya Umum / Referensi"        -> "#F0F9FF";
            case "Seni & Rekreasi"               -> "#FFF0F0";
            default                              -> "#F8FAFC";
        };
    }

    private String singkatKategori(String kategori) {
        if (kategori == null) return "Lainnya";
        return switch (kategori) {
            case "Ilmu Murni / Sains"       -> "Sains";
            case "Teknologi / Ilmu Terapan" -> "Teknologi";
            case "Sejarah & Geografi"       -> "Sejarah";
            case "Karya Umum / Referensi"   -> "Referensi";
            case "Seni & Rekreasi"          -> "Seni";
            default                         -> kategori;
        };
    }

    @FXML
    private void handleLogin() {
        if (AnggotaSession.getIdAnggota() != null) {
            try {
                Stage mainStage = new Stage();
                mainStage.setTitle("Wacana Library - Portal Anggota");
                mainStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/AnggotaMainLayout.fxml"))));
                mainStage.setMaximized(true);
                mainStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Wacana Library — Masuk");
            loginStage.setScene(new Scene(root));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setResizable(false);
            loginStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        if (AnggotaSession.getIdAnggota() != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informasi Sesi Aktif");
            alert.setHeaderText("Anda sudah masuk!");
            alert.setContentText("Sistem mendeteksi Anda masih login sebagai " + AnggotaSession.getNamaAnggota() +
                    ".\nSilakan Keluar (Log Out) dari dasbor terlebih dahulu jika ingin mendaftarkan akun baru.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();
            Stage regStage = new Stage();
            regStage.setTitle("Wacana Library — Daftar Anggota");
            regStage.setScene(new Scene(root));
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.setResizable(false);
            regStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}