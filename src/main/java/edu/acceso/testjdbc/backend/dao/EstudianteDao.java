package edu.acceso.testjdbc.backend.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.testjdbc.backend.Conexion;
import edu.acceso.testjdbc.domain.Centro;
import edu.acceso.testjdbc.domain.Estudiante;

public class EstudianteDao implements GenericDao<Estudiante> {

    private Conexion cx;

    public EstudianteDao(Conexion cx) {
        this.cx = cx;
    }


    @Override
    public Estudiante get(int id) throws DataAccessException {
        String sqlString = "SELECT * FROM Estudiante WHERE id = ?";

        try (
            Connection conn = cx.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? resultSetToEstudiante(rs) : null;
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

    }

    @Override
    public List<Estudiante> get() throws DataAccessException {
        String sqlString = "SELECT * FROM Estudiante";

        List<Estudiante> estudiantes = new ArrayList<>();

        try(
            Connection conn = cx.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
        ) {
            while(rs.next()) {
                try {
                    Estudiante estudiante = resultSetToEstudiante(rs);
                    estudiantes.add(estudiante);
                } catch(SQLException e) {
                    System.err.println("Un registro no puede convertirse en estudiante: " + e.getMessage());
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

        return estudiantes;
    }

    @Override
    public int insert(Estudiante estudiante) throws DataAccessException {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id) VALUES (?, ?, ?, ?)";

        if(estudiante.getId() != null) throw new IllegalArgumentException("El estudiante no puede tener identificador ya fijado");

        try(
            Connection conn = cx.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            setParams(pstmt, estudiante);
            pstmt.executeUpdate();
            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                if(rs.next()) estudiante.setId(rs.getInt(1));
                else assert false: "La base de datos no devolvió identificador para el estudiante";
            }
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

        return estudiante.getId();
    }

    @Override
    public void insert(Iterable<Estudiante> estudiantes) throws DataAccessException {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id) VALUES (?, ?, ?, ?)";

        try (Connection conn = cx.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                for(Estudiante estudiante: estudiantes) {
                    if(estudiante.getId() != null) {
                        System.err.println("El estudiante no puede tener identificador ya fijado");
                        continue;
                    }
                    setParams(pstmt, estudiante);
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
    public boolean remove(int id) throws DataAccessException {
        String sqlString = "DELETE FROM Estudiante WHERE id = ?";

        try (
            Connection conn = cx.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

    }

    @Override
    public void update(Estudiante estudiante) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET nombre = ?, nacimiento = ?, centro = ? WHERE id = ?";

        try(
            Connection conn = cx.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            setParams(pstmt, estudiante);
            int rows = pstmt.executeUpdate();
            if(rows == 0) throw new IllegalArgumentException(String.format("El estudiante con ID %d no existe", estudiante.getId()));
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }

    }

    private void setParams(PreparedStatement pstmt, Estudiante estudiante) throws SQLException {
        pstmt.setString(1, estudiante.getNombre());
        pstmt.setDate(2, Date.valueOf(estudiante.getNacimiento()));
        Integer centroId = estudiante.getCentro() == null ? null : estudiante.getCentro().getId();
        pstmt.setObject(3, centroId, Types.INTEGER);
        pstmt.setObject(4, estudiante.getId(), Types.INTEGER);
    }

    private Estudiante resultSetToEstudiante(ResultSet rs) throws DataAccessException, SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        LocalDate nacimiento;
        nacimiento = rs.getDate("nacimiento").toLocalDate();

        Integer centroId = rs.getInt("centro");
        Centro centro = null;

        if(centroId != null) {
            CentroDao centroDao = new CentroDao(cx);
            centro = centroDao.get(centroId);
        }

        return new Estudiante(id, nombre, nacimiento, centro);
    }
}