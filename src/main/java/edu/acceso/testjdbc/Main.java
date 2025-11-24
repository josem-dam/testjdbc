package edu.acceso.testjdbc;

import java.nio.file.Path;
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

import edu.acceso.testjdbc.domain.Centro;
import edu.acceso.testjdbc.domain.Estudiante;
import edu.acceso.testjdbc.domain.Titularidad;

public class Main {

    public static Centro resultSetToCentro(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        Titularidad titularidad = Titularidad.fromString(rs.getString("titularidad"));
        return new Centro(id, nombre, titularidad);
    }

    public static Estudiante resultSetToEstudiante(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        LocalDate nacimiento = rs.getDate("nacimiento").toLocalDate();
        Integer idCentro = rs.getInt("centro");

        // ¡¡¡¡ Tengo que obtener el centro, no me vale el identificador !!!!!

        Centro centro = null;
        if(rs.wasNull()) idCentro = null;
        else centro = getCentro(idCentro);
        return new Estudiante(id, nombre, nacimiento, centro);
    }

    public static Centro getCentro(int id) throws SQLException {
        String sqlString = "SELECT * FROM Centro WHERE id = ?";
        ConnectionPool cp = ConnectionPool.getInstance();

        try(
            Connection conn = cp.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? resultSetToCentro(rs) : null;
        }
    }

    public static Estudiante getEstudiante(int id) throws SQLException {
        String sqlString = "SELECT * FROM Estudiante WHERE id = ?";
        ConnectionPool cp = ConnectionPool.getInstance();

        try(
            Connection conn = cp.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? resultSetToEstudiante(rs) : null;
        }
    } 

    public static List<Centro> getCentros() throws SQLException {
        String sqlString = "SELECT * FROM Centro";
        ConnectionPool cp = ConnectionPool.getInstance();

        List<Centro> centros = new ArrayList<>();

        try(
            Connection conn = cp.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
        ) {
            // Si falla la creación de un centro,
            // se captura la excepción y se pasa al siguiente.
            while(rs.next()) {
                try {
                    centros.add(resultSetToCentro(rs));
                }
                catch(SQLException e) {
                    // Registrar el error y continuar.
                }
            }
        }

        return centros;
    }

    public static List<Estudiante> getEstudiantes() throws SQLException {
        String sqlString = "SELECT * FROM Estudiante";
        ConnectionPool cp = ConnectionPool.getInstance();

        List<Estudiante> estudiantes = new ArrayList<>();

        try(
            Connection conn = cp.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
        ) {
            while(rs.next()) {
                try {
                    estudiantes.add(resultSetToEstudiante(rs));
                }
                catch(SQLException e) {
                    // Registrar el error y continuar.
                }
            }
        }

        return estudiantes;
    }

    /*
    public static Centro getCentro(int id) throws SQLException {
        String sqlString = "SELECT * FROM Centro WHERE id = ?";
        return GenericQueries.get(id, sqlString, Main::resultSetToCentro);
    }

    public static Estudiante getEstudiante(int id) throws SQLException {
        String sqlString = "SELECT * FROM Estudiante WHERE id = ?";
        return GenericQueries.get(id, sqlString, Main::resultSetToEstudiante);
    }

    public static List<Centro> getCentros() throws SQLException {
        String sqlString = "SELECT * FROM Centro";
        return GenericQueries.getAll(sqlString, Main::resultSetToCentro);
    }   

    public static List<Estudiante> getEstudiantes() throws SQLException {
        String sqlString = "SELECT * FROM Estudiante";
        return GenericQueries.getAll(sqlString, Main::resultSetToEstudiante);
    }
    */

    public static void main(String[] args) {
        final String dbProtocol = "jdbc:sqlite:";

        // Las bases de datos de SQLite son archivos.
        String dbPath = Path.of(System.getProperty("java.io.tmpdir"), "test.db").toString();
        // Alternativa particular de SQLite: base de datos en memoria.
        dbPath = "file::memory:?cache=shared";
        String dbUrl = String.format("%s%s", dbProtocol, dbPath);

        ConnectionPool cp = ConnectionPool.getInstance(dbUrl);

        Centro[] centros = new Centro[] {
            new Centro(11004866, "IES Castillo de Luna", Titularidad.PUBLICA),
            new Centro(11700602, "IES Pintor Juan Lara", Titularidad.PUBLICA),
            new Centro(21002100, "IES Padre José Miravent", Titularidad.PUBLICA)
        };

        
        try(Connection conn = cp.getConnection()) {
            System.out.println("Hemos logrado conectar a la base de datos");

            try(Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");

                stmt.executeUpdate("""
                    CREATE TABLE Centro (
                        id          INTEGER             PRIMARY KEY,
                        nombre      VARCHAR(200)        NOT NULL,
                        titularidad CHAR(7)             CHECK (titularidad IN ('pública','privada'))
                    );
                """);

                stmt.executeUpdate("""
                    CREATE TABLE Estudiante (
                        id             INTEGER       PRIMARY KEY /* GENERATED BY DEFAULT AS IDENTITY */,
                        nombre         VARCHAR(250)  NOT NULL,
                        nacimiento     DATE          NOT NULL,
                        centro         INTEGER,

                        constraint fk_est_cen FOREIGN KEY(centro) REFERENCES Centro(id)
                            ON DELETE SET NULL
                            on UPDATE CASCADE
                    );
                """);

            }

            String sqlString = "INSERT INTO Centro VALUES (?, ?, ?);";
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                for(Centro centro: centros) {
                    pstmt.setInt(1, centro.getId());
                    pstmt.setString(2, centro.getNombre());
                    pstmt.setString(3, centro.getTitularidad().toString());
                    pstmt.executeUpdate();
                }
            }

            List<Centro> centrosRec = getCentros();
            centrosRec.forEach(System.out::println);

            System.out.println("--- *** ---");
            System.out.println(getCentro(11004866));
            System.out.println("--- *** ---");

            Estudiante[] estudiantes = new Estudiante[] {
                new Estudiante(null, "Perico de los Palotes", LocalDate.of(2000, 01, 01), centros[0]),
                new Estudiante(null, "Segismundo Vergara", LocalDate.of(2002, 02, 02), null)
            };

            sqlString = "INSERT INTO Estudiante VALUES (?, ?, ?, ?)";
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                for(Estudiante estudiante: estudiantes) {
                    pstmt.setObject(1, estudiante.getId(), Types.INTEGER);
                    pstmt.setString(2, estudiante.getNombre());
                    Date nacimiento = Date.valueOf(estudiante.getNacimiento());
                    pstmt.setDate(3, nacimiento);
                    Integer idCentro = estudiante.getCentro() == null ? null : estudiante.getCentro().getId();
                    pstmt.setObject(4, idCentro, Types.INTEGER);
                    pstmt.executeUpdate();
                    try(ResultSet rs = pstmt.getGeneratedKeys()) {
                        if(rs.next()) estudiante.setId(rs.getInt(1));
                        else assert false: "La base de datos no devolvió identificador para el estudiante";
                        System.out.printf("'%s' obtiene el identificador %d.\n", estudiante.getNombre(), estudiante.getId());
                    }
                }
            }

            System.out.println("--- *** Estudiantes recuperados *** ---");
            List<Estudiante> estudiantesRec = getEstudiantes();
            estudiantesRec.forEach(System.out::println);
        }
        catch(SQLException err) {
            err.printStackTrace();
            System.err.println("Error de conexión. " + err.getMessage());
        }

        Chorrada.ejecutar();
    }
}
