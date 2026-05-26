package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomepageController {

    @FXML private TextField searchField;
    @FXML private FlowPane  bookGrid;
    @FXML private VBox      emptyState;

    @FXML private Label lblTotalBuku;
    @FXML private Label lblTersedia;
    @FXML private Label lblDipinjam;
    @FXML private Label lblKategori;
    @FXML private Label lblJumlahHasil;

    // Filter chip buttons
    @FXML private Button chipSemua;
    @FXML private Button chipSastra;
    @FXML private Button chipSains;
    @FXML private Button chipTeknologi;
    @FXML private Button chipSejarah;
    @FXML private Button chipBahasa;

    private String activeKategori = "";

    // ────────────────────────────────────────────
    @FXML
    public void initialize() {
        muatStatistik();
        muatBuku("", "");
    }

    // ═══════════════ SEARCH ═══════════════
    @FXML
    private void handleCari() {
        muatBuku(searchField.getText().trim(), activeKategori);
    }

    @FXML
    private void handleCariButton() {
        muatBuku(searchField.getText().trim(), activeKategori);
    }

    // ═══════════════ FILTER CHIPS ═══════════════
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
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #64748B;" +
                "-fx-border-color: #CBD5E1; -fx-border-radius: 999;" +
                "-fx-background-radius: 999; -fx-font-size: 12; -fx-padding: 6 16; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: #185FA5; -fx-text-fill: white;" +
                "-fx-background-radius: 999; -fx-font-size: 12; -fx-padding: 6 16; -fx-cursor: hand;";

        List<Button> chips = List.of(chipSemua, chipSastra, chipSains, chipTeknologi, chipSejarah, chipBahasa);
        for (Button chip : chips) {
            chip.setStyle(chip == active ? activeStyle : inactiveStyle);
        }
    }

    @FXML
    private void scrollToKatalog() {
        // Scroll to catalog — handled by ScrollPane naturally
    }

    // ═══════════════ DATA LOADING ═══════════════
    private void muatStatistik() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rsTotal = stmt.executeQuery("SELECT COUNT(*) AS n FROM Buku");
            if (rsTotal.next()) lblTotalBuku.setText(String.valueOf(rsTotal.getInt("n")));

            ResultSet rsTersedia = stmt.executeQuery("SELECT COUNT(*) AS n FROM Buku WHERE status = 'Tersedia'");
            if (rsTersedia.next()) lblTersedia.setText(String.valueOf(rsTersedia.getInt("n")));

            ResultSet rsDipinjam = stmt.executeQuery("SELECT COUNT(*) AS n FROM Buku WHERE status = 'Dipinjam'");
            if (rsDipinjam.next()) lblDipinjam.setText(String.valueOf(rsDipinjam.getInt("n")));

            ResultSet rsKat = stmt.executeQuery("SELECT COUNT(DISTINCT kategori) AS n FROM Buku");
            if (rsKat.next()) lblKategori.setText(String.valueOf(rsKat.getInt("n")));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void muatBuku(String keyword, String kategori) {
        bookGrid.getChildren().clear();

        List<String[]> bukuList = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT idBuku, judul, pengarang, kategori, tahunTerbit, halaman, status FROM Buku WHERE 1=1");

        if (!keyword.isEmpty()) {
            query.append(" AND (judul LIKE ? OR pengarang LIKE ? OR kategori LIKE ?)");
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
            }
            if (!kategori.isEmpty()) {
                ps.setString(idx, "%" + kategori + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bukuList.add(new String[]{
                            rs.getString("idBuku"),
                            rs.getString("judul"),
                            rs.getString("pengarang"),
                            rs.getString("kategori"),
                            String.valueOf(rs.getInt("tahunTerbit")),
                            String.valueOf(rs.getInt("halaman")),
                            rs.getString("status")
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

    // ═══════════════ BOOK CARD UI ═══════════════
    private VBox buatKartuBuku(String[] buku) {
        // buku: [0]=id, [1]=judul, [2]=pengarang, [3]=kategori, [4]=tahun, [5]=halaman, [6]=status
        String idBuku      = buku[0];
        String judul       = buku[1];
        String pengarang   = buku[2];
        String kategori    = buku[3];
        String tahun       = buku[4];
        String status      = buku[6];

        // ── Card container ──
        VBox card = new VBox(0);
        card.setPrefWidth(175);
        card.setMaxWidth(175);
        card.setStyle("-fx-background-color: white;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // ── Cover ──
        StackPane cover = new StackPane();
        cover.setPrefHeight(115);
        cover.setStyle("-fx-background-color: " + coverColor(kategori) + ";" +
                "-fx-background-radius: 10 10 0 0;");

        Label emoji = new Label(coverEmoji(kategori));
        emoji.setStyle("-fx-font-size: 36;");

        // Status badge
        Label badge = new Label(status);
        boolean tersedia = "Tersedia".equalsIgnoreCase(status);
        badge.setStyle("-fx-background-color: " + (tersedia ? "#DCFCE7" : "#FEF3C7") + ";" +
                "-fx-text-fill: " + (tersedia ? "#15803D" : "#92400E") + ";" +
                "-fx-font-size: 10; -fx-font-weight: bold;" +
                "-fx-background-radius: 999; -fx-padding: 2 8;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(8, 8, 0, 0));

        cover.getChildren().addAll(emoji, badge);

        // ── Info section ──
        VBox info = new VBox(4);
        info.setPadding(new Insets(10, 12, 12, 12));

        // Judul (max 2 lines)
        Label lblJudul = new Label(judul);
        lblJudul.setWrapText(true);
        lblJudul.setMaxHeight(38);
        lblJudul.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1B2A4A;" +
                "-fx-wrap-text: true;");

        // Pengarang
        Label lblPengarang = new Label(pengarang);
        lblPengarang.setStyle("-fx-font-size: 11; -fx-text-fill: #64748B;");
        lblPengarang.setWrapText(true);
        lblPengarang.setMaxHeight(28);

        // Bottom row: kategori chip + tahun
        HBox bottom = new HBox(6);
        bottom.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(bottom, new Insets(4, 0, 0, 0));

        Label lblKat = new Label(singkatKategori(kategori));
        lblKat.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8;" +
                "-fx-font-size: 10; -fx-background-radius: 999; -fx-padding: 2 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTahun = new Label(tahun);
        lblTahun.setStyle("-fx-font-size: 10; -fx-text-fill: #94A3B8;");

        bottom.getChildren().addAll(lblKat, spacer, lblTahun);
        info.getChildren().addAll(lblJudul, lblPengarang, bottom);

        card.getChildren().addAll(cover, info);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
                "-fx-border-color: #E2E8F0", "-fx-border-color: #93C5FD")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
                "-fx-border-color: #93C5FD", "-fx-border-color: #E2E8F0")));

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

    private String coverEmoji(String kategori) {
        if (kategori == null) return "📚";
        return switch (kategori) {
            case "Sastra"                        -> "📖";
            case "Ilmu Murni / Sains"            -> "🔬";
            case "Teknologi / Ilmu Terapan"      -> "💻";
            case "Sejarah & Geografi"            -> "🌍";
            case "Bahasa"                        -> "🔤";
            case "Ilmu Sosial"                   -> "👥";
            case "Karya Umum / Referensi"        -> "📕";
            case "Seni & Rekreasi"               -> "🎨";
            default                              -> "📚";
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

    // ═══════════════ LOGIN / REGISTER ═══════════════
    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
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
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Register.fxml"));
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
