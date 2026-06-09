package id.ac.ukdw.rplbo.wacanalibrary.controllers;

import id.ac.ukdw.rplbo.wacanalibrary.dao.AnggotaDao;
import id.ac.ukdw.rplbo.wacanalibrary.dao.BukuDao;
import id.ac.ukdw.rplbo.wacanalibrary.dao.TransaksiDao;
import id.ac.ukdw.rplbo.wacanalibrary.dao.impl.AnggotaDaoImpl;
import id.ac.ukdw.rplbo.wacanalibrary.dao.impl.BukuDaoImpl;
import id.ac.ukdw.rplbo.wacanalibrary.dao.impl.TransaksiDaoImpl;
import id.ac.ukdw.rplbo.wacanalibrary.models.Anggota;
import id.ac.ukdw.rplbo.wacanalibrary.models.Buku;
import id.ac.ukdw.rplbo.wacanalibrary.models.Transaksi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;



public class FormPengembalianController {

    @FXML private Label lblNamaAnggota, lblIdAnggota, lblJudulBuku, lblIdBuku;
    @FXML private Label lblTglPinjam, lblJatuhTempo, lblTotalDenda, lblHariTelat, lblInstruksiQris;
    @FXML private DatePicker dpTglKembali;
    @FXML private ComboBox<String> cbMetodeBayar;
    @FXML private VBox boxDenda;

    private Transaksi transaksiAktif;
    private double totalDendaKalkulasi = 0.0;
    private final double TARIF_DENDA = 1000.0;

    // Instansiasi DAO
    private final AnggotaDao anggotaDao = new AnggotaDaoImpl();
    private final BukuDao bukuDao = new BukuDaoImpl();
    private final TransaksiDao transaksiDao = new TransaksiDaoImpl();

    public void setTransaksiData(Transaksi trx) {
        this.transaksiAktif = trx;

        lblIdAnggota.setText("ID: " + trx.getIdAnggota());
        lblIdBuku.setText("ID: " + trx.getIdBuku());
        lblTglPinjam.setText(trx.getTanggalPinjam());
        lblJatuhTempo.setText(trx.getTanggalJatuhTempo());

        // Mengambil detail nama & judul buku menggunakan DAO
        muatDetailDariDB(trx.getIdAnggota(), trx.getIdBuku());

        cbMetodeBayar.setItems(FXCollections.observableArrayList("Cash", "QRIS"));
        cbMetodeBayar.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblInstruksiQris.setVisible("QRIS".equals(newVal));
        });

        dpTglKembali.setValue(LocalDate.now());
        hitungDendaInteraktif(LocalDate.now());

        dpTglKembali.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) hitungDendaInteraktif(newValue);
        });
    }

    private void hitungDendaInteraktif(LocalDate tanggalDikembalikan) {
        LocalDate jatuhTempo = LocalDate.parse(transaksiAktif.getTanggalJatuhTempo());
        long selisihHari = ChronoUnit.DAYS.between(jatuhTempo, tanggalDikembalikan);

        if (selisihHari > 0) {
            totalDendaKalkulasi = selisihHari * TARIF_DENDA;
            lblHariTelat.setText("Terlambat: " + selisihHari + " Hari");
            lblTotalDenda.setText("Rp " + (int) totalDendaKalkulasi);
            boxDenda.setVisible(true);
            boxDenda.setManaged(true);
        } else {
            totalDendaKalkulasi = 0.0;
            boxDenda.setVisible(false);
            boxDenda.setManaged(false);
        }
    }

    private void muatDetailDariDB(String idAnggota, String idBuku) {
        Anggota anggota = anggotaDao.getAnggotaById(idAnggota);
        if (anggota != null) {
            lblNamaAnggota.setText(anggota.namaLengkapProperty().get());
        }

        Buku buku = bukuDao.getBukuById(idBuku);
        if (buku != null) {
            lblJudulBuku.setText(buku.judulProperty().get());
        }
    }

    @FXML
    private void handleKembalikan() {
        if (totalDendaKalkulasi > 0 && cbMetodeBayar.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Silakan pilih metode pembayaran denda terlebih dahulu!").showAndWait();
            return;
        }

        try {
            // 1. Update Transaksi melalui DAO
            String statusTrx = totalDendaKalkulasi > 0 ? "Terlambat" : "Selesai";
            String tglKembali = dpTglKembali.getValue().toString();
            String metode = totalDendaKalkulasi > 0 ? cbMetodeBayar.getValue() : "-";

            transaksiDao.prosesPengembalian(transaksiAktif.getIdTransaksi(), statusTrx, tglKembali, totalDendaKalkulasi, metode);

            // 2. Kembalikan status buku di Katalog melalui DAO
            Buku buku = bukuDao.getBukuById(transaksiAktif.getIdBuku());
            if (buku != null) {
                buku.statusProperty().set("Tersedia");
                bukuDao.updateBuku(buku); // Menyimpan perubahan ke DB
            }

            // 3. Tambahkan kembali kuota pinjam anggota (+1) melalui DAO
            anggotaDao.tambahBatasPinjam(transaksiAktif.getIdAnggota(), 1);

            tutupJendela();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pengembalian Berhasil");
            alert.setHeaderText(null);

            String pesan = "Buku berhasil dikembalikan ke perpustakaan.\n";
            if (totalDendaKalkulasi > 0) {
                pesan += "Status Denda: Lunas (via " + cbMetodeBayar.getValue() + ")";
            } else {
                pesan += "Status Denda: Tidak ada denda keterlambatan.";
            }
            alert.setContentText(pesan);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error Database");
            err.setHeaderText("Gagal Menyimpan Data!");
            err.setContentText("Pesan Error: " + e.getMessage());
            err.showAndWait();
        }
    }

    @FXML
    private void handleBatal() { tutupJendela(); }

    private void tutupJendela() {
        Stage stage = (Stage) lblNamaAnggota.getScene().getWindow();
        stage.close();
    }
}