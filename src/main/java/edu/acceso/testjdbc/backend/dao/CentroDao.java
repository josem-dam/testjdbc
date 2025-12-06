package edu.acceso.testjdbc.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.testjdbc.domain.Centro;
import edu.acceso.testjdbc.domain.Titularidad;

public class CentroDao implements GenericDao<Centro> {

    private DataSource ds;

    public CentroDao(DataSource ds) {
        this.ds = ds;
    }

    private static Centro resultSetToCentro(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        Titularidad titularidad = Titularidad.fromString(rs.getString("titularidad"));
        return new Centro(id, nombre, titularidad);
    }

    @Override
    public Centro get(int id) throws DataAccessException {
        String sqlString = "SELECT * FROM Centro WHERE id = ?";

        try(
            Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? resultSetToCentro(rs) : null;
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public List<Centro> get() throws DataAccessException {
        String sqlString = "SELECT * FROM Centro";

        List<Centro> centros = new ArrayList<>();

        try(
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
        ) {
            // ¡¡ Cuidado con que falle la creación de un centro !!
            while(rs.next()) {
                try {
                    Centro centro = resultSetToCentro(rs);
                    centros.add(centro);
                } catch(SQLException e) {
                    System.err.println("Un registro no puede convertirse en centro: " + e.getMessage());
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

        return centros;
    }

    @Override
    public boolean remove(int id) throws DataAccessException {
        String sqlString = "DELETE FROM Centro WHERE id = ?";

        try(
            Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Centro centro) throws SQLException {
        pstmt.setString(1, centro.getNombre());
        pstmt.setString(2, centro.getTitularidad().toString());
        pstmt.setInt(3, centro.getId());
    }

    @Override
    public int insert(Centro centro) throws DataAccessException {
        String sqlString = "INSERT INTO Centro (nombre, titularidad, id) VALUES (?, ?, ?)";

        try (
            Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            setParams(pstmt, centro);
            pstmt.executeUpdate();
            return centro.getId();
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

    }

    @Override
    public void insert(Iterable<Centro> centros) throws DataAccessException {
        String sqlString = "INSERT INTO Centro (nombre, titularidad, id) VALUES (?, ?, ?)";

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                for(Centro centro: centros) {
                    setParams(pstmt, centro);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            } catch(SQLException err) {
                err.printStackTrace();
                conn.rollback();
                throw err;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void update(Centro centro) throws DataAccessException {
        String sqlString = "UPDATE Centro SET nombre = ?, titularidad = ? WHERE id = ?";

        try(
            Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            setParams(pstmt, centro);
            int rows = pstmt.executeUpdate();
            if(rows == 0) throw new IllegalArgumentException(String.format("El centro con ID %d no existe", centro.getId()));
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
