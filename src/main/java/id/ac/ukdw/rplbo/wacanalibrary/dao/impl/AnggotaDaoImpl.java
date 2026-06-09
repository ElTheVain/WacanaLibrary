package id.ac.ukdw.rplbo.wacanalibrary.dao.impl;

import id.ac.ukdw.rplbo.wacanalibrary.dao.AnggotaDao;
import id.ac.ukdw.rplbo.wacanalibrary.models.Anggota;
import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnggotaDaoImpl implements AnggotaDao {

    @Override
    public List<Anggota> getAllAnggota() {
        List<Anggota> listAnggota = new ArrayList<>();
        String query = "SELECT * FROM Anggota";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                listAnggota.add(mapResultSetToAnggota(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listAnggota;
    }

    @Override
    public Anggota getAnggotaById(String idAnggota) {
        String query = "SELECT * FROM Anggota WHERE idAnggota = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idAnggota);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAnggota(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void tambahAnggota(Anggota anggota) {
        String query = "INSERT INTO Anggota (idAnggota, nim, username, namaLengkap, tipe, batasPinjam, aktifSejak, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, anggota.idAnggotaProperty().get());
            ps.setString(2, anggota.nimProperty().get());
            ps.setString(3, anggota.nimProperty().get()); // username = nim (default)
            ps.setString(4, anggota.namaLengkapProperty().get());
            ps.setString(5, anggota.tipeProperty().get());
            ps.setInt(6, anggota.batasPinjamProperty().get());
            ps.setString(7, anggota.aktifSejakProperty().get());
            ps.setString(8, anggota.statusProperty().get());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void updateAnggota(Anggota anggota) {
        String query = "UPDATE Anggota SET nim=?, namaLengkap=?, tipe=?, batasPinjam=?, status=? WHERE idAnggota=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, anggota.nimProperty().get());
            ps.setString(2, anggota.namaLengkapProperty().get());
            ps.setString(3, anggota.tipeProperty().get());
            ps.setInt(4, anggota.batasPinjamProperty().get());
            ps.setString(5, anggota.statusProperty().get());
            ps.setString(6, anggota.idAnggotaProperty().get());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void hapusAnggota(String idAnggota) {
        String query = "DELETE FROM Anggota WHERE idAnggota = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idAnggota);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Anggota> cariAnggota(String keyword) {
        List<Anggota> list = new ArrayList<>();
        String query = "SELECT * FROM Anggota WHERE namaLengkap LIKE ? OR nim LIKE ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAnggota(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void tambahBatasPinjam(String idAnggota, int jumlahPenambahan) {
        String query = "UPDATE Anggota SET batasPinjam = batasPinjam + ? WHERE idAnggota = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, jumlahPenambahan);
            ps.setString(2, idAnggota);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Anggota mapResultSetToAnggota(ResultSet rs) throws SQLException {
        return new Anggota(
                rs.getString("idAnggota"),
                rs.getString("nim"),
                rs.getString("namaLengkap"),
                rs.getString("tipe"),
                rs.getInt("batasPinjam"),
                rs.getString("aktifSejak"),
                rs.getString("status")
        );
    }
}
