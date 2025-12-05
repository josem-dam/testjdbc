package edu.acceso.testjdbc;

import java.io.IOException;
import java.time.LocalDate;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.testjdbc.backend.Conexion;
import edu.acceso.testjdbc.backend.dao.CentroDao;
import edu.acceso.testjdbc.backend.dao.EstudianteDao;
import edu.acceso.testjdbc.domain.Centro;
import edu.acceso.testjdbc.domain.Estudiante;
import edu.acceso.testjdbc.domain.Titularidad;

public class Main {

    public static void main(String[] args) {
        Logger hikariLogger = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
        hikariLogger.setLevel(Level.WARN);

        String url = "file::memory:?cache=shared";

        Conexion cx = null;
        
        try {
            cx = Conexion.create(url, "resources:/centros.sql");
            System.out.println("Hemos logrado conectar a la base de datos");
        } catch(IOException e) {
            System.err.println("Es imposible acceder a la base de datos");
        } catch(DataAccessException e) {
            System.err.println("Error al inicializar la base de datos. " + e.getMessage());
        }

        Centro[] centros = new Centro[] {
            new Centro(11004866, "IES Castillo de Luna", Titularidad.PUBLICA),
            new Centro(11700602, "IES Pintor Juan Lara", Titularidad.PUBLICA),
            new Centro(21002100, "IES Padre José Miravent", Titularidad.PUBLICA)
        };

        
        try {
            CentroDao centroDao = new CentroDao(cx);
            EstudianteDao estudianteDao = new EstudianteDao(cx);

            // Agrego los centros a la base de datos.
            centroDao.insert(centros);

            // Compruebo centros.
            System.out.println("--- Lista de centros ---");
            centroDao.get().forEach(System.out::println);
            System.out.println("--- *** ---");

            System.out.println("--- *** ---");
            System.out.println(centroDao.get(11004866));
            System.out.println("--- *** ---");

            Estudiante[] estudiantes = new Estudiante[] {
                new Estudiante(null, "Perico de los Palotes", LocalDate.of(2000, 01, 01), centros[0]),
                new Estudiante(null, "Segismundo Vergara", LocalDate.of(2002, 02, 02), null)
            };

            estudianteDao.insert(estudiantes);

            System.out.println("--- Lista de estudiantes ---");
            for(Estudiante estudiante: estudianteDao.get()) {
                System.out.printf("Estudiante %d: %s.\n", estudiante.getId(), estudiante);
            }
            System.out.println("--- *** ---");
        }
        catch(DataAccessException err) {
            err.printStackTrace();
            System.err.println("Error de conexión. " + err.getMessage());
        }
    }
}
