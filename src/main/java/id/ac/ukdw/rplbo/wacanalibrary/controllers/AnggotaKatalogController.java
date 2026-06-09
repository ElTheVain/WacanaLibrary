package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import id.ac.ukdw.rplbo.wacanalibrary.utils.AnggotaSession;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnggotaKatalogController {

    @FXML private TextField searchField;
    @FXML private FlowPane  bookGrid;

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            muatBuku(newVal.trim());
        });
        muatBuku("");
    }

    @FXML
    private void handleCari() {
        muatBuku(searchField.getText().trim());
    }

    private void muatBuku(String keyword) {
        bookGrid.getChildren().clear();
        List<String[]> bukuList = new ArrayList<>();

        String query = "SELECT idBuku, isbn, judul, pengarang, kategori, tahunTerbit, halaman, gambar, status " +
                "FROM Buku WHERE judul LIKE ? OR pengarang LIKE ? OR kategori LIKE ? OR isbn LIKE ? ORDER BY judul ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bukuList.add(new String[]{
                            rs.getString("idBuku"),
                            rs.getString("isbn"),
                            rs.getString("judul"),
                            rs.getString("pengarang"),
                            rs.getString("kategori"),
                            String.valueOf(rs.getInt("tahunTerbit")),
                            String.valueOf(rs.getInt("halaman")),
                            rs.getString("gambar"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (String[] buku : bukuList) {
            bookGrid.getChildren().add(buatKartuBuku(buku));
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
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: default; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

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

        VBox info = new VBox(4);
        info.setPadding(new Insets(10, 12, 12, 12));

        Label lblJudul = new Label(judul);
        lblJudul.setWrapText(true);
        lblJudul.setMaxHeight(38);
        lblJudul.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1B2A4A;");

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

        // Label Status Blok Bawah
        Label lblStatusBawah = new Label(labelStatusTampil.toUpperCase());
        lblStatusBawah.setMaxWidth(Double.MAX_VALUE);
        lblStatusBawah.setAlignment(Pos.CENTER);

        if (tersedia) {
            lblStatusBawah.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6;");
        } else {
            lblStatusBawah.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6;");
        }

        VBox.setMargin(lblStatusBawah, new Insets(8, 0, 0, 0));
        info.getChildren().add(lblStatusBawah);

        card.getChildren().addAll(cover, info);
        return card;
    }

    private String coverColor(String kategori) {
        if (kategori == null) return "#F1F5F9";
        return switch (kategori) {
            case "Sastra" -> "#FFF7ED";
            case "Ilmu Murni / Sains" -> "#F0FDF4";
            case "Teknologi / Ilmu Terapan" -> "#EFF6FF";
            case "Sejarah & Geografi" -> "#FFFBEB";
            case "Bahasa" -> "#FDF4FF";
            case "Ilmu Sosial" -> "#FFF1F2";
            case "Karya Umum / Referensi" -> "#F0F9FF";
            case "Seni & Rekreasi" -> "#FFF0F0";
            default -> "#F8FAFC";
        };
    }

    private String singkatKategori(String kategori) {
        if (kategori == null) return "Lainnya";
        return switch (kategori) {
            case "Ilmu Murni / Sains" -> "Sains";
            case "Teknologi / Ilmu Terapan" -> "Teknologi";
            case "Sejarah & Geografi" -> "Sejarah";
            case "Karya Umum / Referensi" -> "Referensi";
            case "Seni & Rekreasi" -> "Seni";
            default -> kategori;
        };
    }
}