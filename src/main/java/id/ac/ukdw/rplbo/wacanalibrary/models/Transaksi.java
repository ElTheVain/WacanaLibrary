package id.ac.ukdw.rplbo.wacanalibrary.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Transaksi {
    private final StringProperty idTransaksi;
    private final StringProperty idAnggota;
    private final StringProperty idBuku;
    private final StringProperty tanggalPinjam;
    private final StringProperty tanggalJatuhTempo;
    private final StringProperty statusTransaksi;
    private final StringProperty metodePembayaran;

    public Transaksi(String idTransaksi, String idAnggota, String idBuku, String tanggalPinjam, String tanggalJatuhTempo, String statusTransaksi, String metodePembayaran) {
        this.idTransaksi = new SimpleStringProperty(idTransaksi);
        this.idAnggota = new SimpleStringProperty(idAnggota);
        this.idBuku = new SimpleStringProperty(idBuku);
        this.tanggalPinjam = new SimpleStringProperty(tanggalPinjam);
        this.tanggalJatuhTempo = new SimpleStringProperty(tanggalJatuhTempo);
        this.statusTransaksi = new SimpleStringProperty(statusTransaksi);
        this.metodePembayaran = new SimpleStringProperty(metodePembayaran);
    }

    public StringProperty idTransaksiProperty() { return idTransaksi; }
    public StringProperty idAnggotaProperty() { return idAnggota; }
    public StringProperty idBukuProperty() { return idBuku; }
    public StringProperty tanggalPinjamProperty() { return tanggalPinjam; }
    public StringProperty tanggalJatuhTempoProperty() { return tanggalJatuhTempo; }
    public StringProperty statusTransaksiProperty() { return statusTransaksi; }
    public StringProperty metodePembayaranProperty() { return metodePembayaran; }

    public String getIdTransaksi() { return idTransaksi.get(); }
    public String getIdAnggota() { return idAnggota.get(); }
    public String getIdBuku() { return idBuku.get(); }
    public String getTanggalPinjam() { return tanggalPinjam.get(); }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo.get(); }
    public String getStatusTransaksi() { return statusTransaksi.get(); }
    public String getMetodePembayaran() { return metodePembayaran.get(); }
}