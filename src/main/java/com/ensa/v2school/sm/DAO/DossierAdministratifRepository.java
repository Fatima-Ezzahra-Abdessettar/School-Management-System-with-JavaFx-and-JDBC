package com.ensa.v2school.sm.DAO;

import com.ensa.v2school.sm.Models.DossierAdministratif;
import com.ensa.v2school.sm.utils.DataBaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DossierAdministratifRepository implements CRUD<DossierAdministratif, Integer> {

    private final DataBaseConnection connection;

    public DossierAdministratifRepository() {
        this.connection = DataBaseConnection.getInstance();
    }

    @Override
    public DossierAdministratif create(DossierAdministratif dossier) throws SQLException {
        String sql = """
            INSERT INTO dossier_administratif
            (numero_inscription, date_creation, eleve_id)
            VALUES (?, ?, ?)
        """;

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, dossier.getNumeroInscription());
            ps.setDate(2, Date.valueOf(dossier.getDateCreation()));
            ps.setString(3, dossier.getEleveId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dossier.setId(rs.getInt(1));
                }
            }
            return dossier;
        }
    }

    @Override
    public Optional<DossierAdministratif> get(Integer id) throws SQLException {
        String sql = "SELECT * FROM dossier_administratif WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DossierAdministratif> getAll() throws SQLException {
        String sql = "SELECT * FROM dossier_administratif ORDER BY date_creation DESC";
        List<DossierAdministratif> list = new ArrayList<>();

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public DossierAdministratif delete(DossierAdministratif dossier) throws SQLException {
        String sql = "DELETE FROM dossier_administratif WHERE id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dossier.getId());
            ps.executeUpdate();
            return dossier;
        }
    }

    public DossierAdministratif update(DossierAdministratif dossier) throws SQLException {
        String sql = """
            UPDATE dossier_administratif
            SET numero_inscription = ?
            WHERE id = ?
        """;

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dossier.getNumeroInscription());
            ps.setInt(2, dossier.getId());
            ps.executeUpdate();
            return dossier;
        }
    }

    public Optional<DossierAdministratif> findByStudentId(String studentId) throws SQLException {
        String sql = "SELECT * FROM dossier_administratif WHERE eleve_id = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<DossierAdministratif> findByNumeroInscription(String numero) throws SQLException {
        String sql = "SELECT * FROM dossier_administratif WHERE numero_inscription = ?";

        try (Connection con = connection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, numero);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public String generateNumeroInscription() throws SQLException {
        String year = String.valueOf(LocalDate.now().getYear());
        String sql = "SELECT COALESCE(MAX(id), 0) FROM dossier_administratif";

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                int next = rs.getInt(1) + 1;
                return String.format("INS-%s-%04d", year, next);
            }
        }
        return String.format("INS-%s-0001", year);
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_administratif";

        try (Connection con = connection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private DossierAdministratif map(ResultSet rs) throws SQLException {
        return new DossierAdministratif(
                rs.getInt("id"),
                rs.getString("numero_inscription"),
                rs.getDate("date_creation").toLocalDate(),
                rs.getString("eleve_id"),
                null
        );
    }
}
