package edu.acceso.testjdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Chorrada {

    public static void ejecutar() {
        ConnectionPool cp = ConnectionPool.getInstance();

        try(Connection conn = cp.getConnection()) {
            String sqlString = "SELECT count(*) FROM Estudiante";
            try(
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlString);
            ) {
                if(rs.next()) {
                    int cantidad = rs.getInt(1);
                    System.out.printf("Cantidad de estudiantes en la base de datos: %d.\f", cantidad);
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
