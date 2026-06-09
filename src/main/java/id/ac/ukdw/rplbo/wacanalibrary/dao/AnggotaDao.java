package id.ac.ukdw.rplbo.wacanalibrary.dao;

import id.ac.ukdw.rplbo.wacanalibrary.models.Anggota;
import java.util.List;

public interface AnggotaDao {
    List<Anggota> getAllAnggota();
    Anggota getAnggotaById(String idAnggota);
    void tambahAnggota(Anggota anggota);
    void updateAnggota(Anggota anggota);
    void hapusAnggota(String idAnggota);
    List<Anggota> cariAnggota(String keyword);

    void tambahBatasPinjam(String idAnggota, int jumlahPenambahan);
}