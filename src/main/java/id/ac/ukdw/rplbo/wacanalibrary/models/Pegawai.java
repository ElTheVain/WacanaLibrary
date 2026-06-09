package id.ac.ukdw.rplbo.wacanalibrary.models;

public class Pegawai {
    private String idPegawai;
    private String username;
    private String role;

    public Pegawai(String idPegawai, String username, String role) {
        this.idPegawai = idPegawai;
        this.username = username;
        this.role = role;
    }

    public String getIdPegawai() { return idPegawai; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
