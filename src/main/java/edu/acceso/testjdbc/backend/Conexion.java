package edu.acceso.testjdbc.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.acceso.sqlutils.ConnectionPool;
import edu.acceso.sqlutils.SqlUtils;
import edu.acceso.sqlutils.errors.DataAccessException;

public class Conexion {

    private static Conexion instance;

    private final ConnectionPool cp;
    private static final String sgbd = "jdbc:sqlite:";
    private static final String tabla = "Centro";

    private Conexion(String path) {
        String url = sgbd + path;
        cp = ConnectionPool.getInstance(url);
    }

    public static Conexion create(String path) {
        try {
            return create(path, null);
        } catch(IOException | DataAccessException e) {
            assert false: "No pueden generarse errores si no se inicializa la base de datos.";
            return null;
        }
    }

    public static Conexion create(String path, String guion) throws IOException, DataAccessException {
        if(instance != null) throw new IllegalStateException("La conexión ya se inicializó");

        instance = new Conexion(path);
        if(guion != null) instance.inicializar(guion);

        return instance;
    }

    public static Conexion getInstance() {
        if(instance == null) throw new IllegalStateException("Debe crearse la conexión primero");

        return instance;
    }

    private InputStream obtenerEntrada(String guion) throws IOException {
        if(guion.startsWith("resources:")) {
            guion = guion.substring("resources:".length());
            return getClass().getResourceAsStream(guion);
        }
        else {
            try {
                URL url = new URI(guion).toURL();
                return url.openStream();
            } catch(URISyntaxException e) {
                throw new IllegalArgumentException("La URL del guión es inválida");
            } 
        }
    }

    private void inicializar(String guion) throws IOException, DataAccessException {
        try(
            InputStream st = obtenerEntrada(guion);
            Connection conn = getConnection();
        ) {
            try(Statement stmt = conn.createStatement()) {
                stmt.executeQuery(String.format("SELECT 1 FROM %s", tabla));
            } catch(SQLException e) {
                try {
                    SqlUtils.executeSQL(conn, st);
                } catch(SQLException ex) {
                    throw new DataAccessException("Error al inicializar la base de datos", ex);
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("Imposible conectar a la base de datos", e);
        }
    }

    public ConnectionPool getConnectionPool() {
        return cp;
    }

    public Connection getConnection() throws DataAccessException {
        try {
            return cp.getConnection();
        } catch(SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
