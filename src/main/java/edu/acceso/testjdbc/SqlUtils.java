package edu.acceso.testjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SqlUtils {

    /**
     * Implementa un iterador a partir de un ResultSet.
     */
    private static class ResultSetIterator implements Iterator<ResultSet> {

        private final ResultSet rs;
        private boolean avanzar;
        private boolean hasNextElement;

        public ResultSetIterator(ResultSet rs) {
            this.rs = rs;
            avanzar = true;
        }

        @Override
        public boolean hasNext() {
            if(avanzar) {
                try {
                    if(rs.isClosed()) {
                        throw new DataAccessRuntimeException("ResultSet is closed!!!");
                    }
                    hasNextElement = rs.next();
                }
                catch(SQLException err) {
                    throw new DataAccessRuntimeException(err);
                }
                finally {
                    avanzar = false;
                }
            }
            return hasNextElement;
        }


        @Override
        public ResultSet next() {
            avanzar = true;
            return rs;
        }
    }
    
    /**
     * Como Function<T, R> pero permite propagar una SQLException.
     */
    @FunctionalInterface
    public static interface CheckedFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    /**
     * Transforma el SQLException que propaga una CheckedFUnction en un DataAccessException, que es una excepción
     * que no necesita ser declarada.
     * @param <T> El tipo que devuelve CheckedFunction.
     * @param checked Un "función" CheckedFunction.
     * @return Una "función" que ha sustituido SQLException por DataAccessException.
     */
    public static <T> Function<ResultSet, T> checkedToUnchecked(CheckedFunction<ResultSet, T> checked) {
        return t -> {
            try {
                return checked.apply(t);
            }
            catch(SQLException err) {
                throw new DataAccessRuntimeException(err);
            }
        };
    }

    /**
     * Genera un flujo con las filas generadas en un ResultSet.
     * @param ac  La sentencia que generó rs o la conexión sobre la que se
     * 	          ejecutó la sentencia. Proporciónese una u otra dependiendo de
     * 	          qué es lo que quiere cerrar automáticamente al cerrarse el
     * 	          Stream resultante.
     * @param rs Los resutados de una consulta.
     * @return Un flujo en el que cada elemento es el siguiente estado del ResultSet proporcionado.
     * @throws SQLException Cuando se produce un error al realizar la consulta.
     */
    public static Stream<ResultSet> resultSetToStream(AutoCloseable ac, ResultSet rs) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ResultSetIterator(rs), Spliterator.ORDERED), false)
            .onClose(() -> {
                try {
                    rs.close();
                    ac.close();
                }
                catch(Exception err) {
                    throw new DataAccessRuntimeException(err);
                }
            });
    }

    /**
     * Genera un flujo de objetos derivados del resultado de una consulta.
     * @param <T> La clase del objeto.
     * @param ac  La sentencia que generó rs o la conexión sobre la que se
     * 	          ejecutó la sentencia. Proporciónese una u otra dependiendo de
     * 	          qué es lo que quiere cerrar automáticamente al cerrarse el
     * 	          Stream resultante.
     * @param rs  El objeto que representa los resultado de la consulta.
     * @param mapper La función que permite transformar la fila en un objeto (puede generar un SQLException).
     * @return El flujo de objetos.
     * @throws SQLException Cuando Cuando se produce un error al realizar la consulta.
     */
    public static <T> Stream<T> resultSetToStream(AutoCloseable ac, ResultSet rs, CheckedFunction<ResultSet, T> mapper) {
        return resultSetToStream(ac, rs).map(checkedToUnchecked(mapper));
    }
}
