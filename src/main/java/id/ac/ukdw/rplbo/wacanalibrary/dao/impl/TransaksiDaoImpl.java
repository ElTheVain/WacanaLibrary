package id.ac.ukdw.rplbo.wacanalibrary.dao.impl;

import id.ac.ukdw.rplbo.wacanalibrary.dao.TransaksiDao;
import id.ac.ukdw.rplbo.wacanalibrary.models.Transaksi;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDaoImpl implements TransaksiDao {

    @Override
    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> list = new ArrayList<>();
        String query = "SELECT t.idTransaksi, t.idAnggota, tb.idBuku, t.tanggalPinjam, t.tanggalJatuhTempo, t.statusTransaksi, t.metodePembayaran " +
                "FROM Transaksi t " +
                "LEFT JOIN Transaksi_Buku tb ON t.idTransaksi = tb.idTransaksi";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tglPinjam = rs.getString("tanggalPinjam");
                String jatuhTempo = rs.getString("tanggalJatuhTempo");

                list.add(new Transaksi(
                        rs.getString("idTransaksi"),
                        rs.getString("idAnggota"),
                        rs.getString("idBuku") == null ? "-" : rs.getString("idBuku"),
                        (tglPinjam != null && !tglPinjam.isEmpty()) ? tglPinjam : LocalDate.now().toString(),
                        (jatuhTempo != null && !jatuhTempo.isEmpty()) ? jatuhTempo : LocalDate.now().toString(),
                        rs.getString("statusTransaksi"),
                        rs.getString("metodePembayaran") == null ? "-" : rs.getString("metodePembayaran")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void prosesPengembalian(String idTransaksi, String status, String tglKembali, double totalDenda, String metodePembayaran) {
        String query = "UPDATE Transaksi SET statusTransaksi = ?, tanggalKembali = ?, totalDenda = ?, metodePembayaran = ? WHERE idTransaksi = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setString(2, tglKembali);
            ps.setDouble(3, totalDenda);
            ps.setString(4, metodePembayaran);
            ps.setString(5, idTransaksi);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}