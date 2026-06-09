package id.ac.ukdw.rplbo.wacanalibrary.models;
import javafx.beans.property.*;

public class Anggota {
    private final StringProperty idAnggota, nim, namaLengkap, tipe, aktifSejak, status;
    private final IntegerProperty batasPinjam;

    public Anggota(String id, String nim, String nama, String tipe, int batas, String aktifSejak, String status) {
        this.idAnggota = new SimpleStringProperty(id);
        this.nim = new SimpleStringProperty(nim != null ? nim : "-");
        this.namaLengkap = new SimpleStringProperty(nama);
        this.tipe = new SimpleStringProperty(tipe);
        this.batasPinjam = new SimpleIntegerProperty(batas);
        this.aktifSejak = new SimpleStringProperty(aktifSejak);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty idAnggotaProperty() { return idAnggota; }
    public StringProperty nimProperty() { return nim; }
    public StringProperty namaLengkapProperty() { return namaLengkap; }
    public StringProperty tipeProperty() { return tipe; }
    public IntegerProperty batasPinjamProperty() { return batasPinjam; }
    public StringProperty aktifSejakProperty() { return aktifSejak; }
    public StringProperty statusProperty() { return status; }
}