package edu.acceso.testjdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericQueries {

    @FunctionalInterface
    public static interface ResultSetConverter<T> {
        T convert(ResultSet rs) throws SQLException;
    }

    public static <T> T get(int id, String sqlString, ResultSetConverter<T> converter) throws SQLException {
        ConnectionPool cp = ConnectionPool.getInstance();
        try(
            Connection conn = cp.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? converter.convert(rs) : null;
        }
    
    }
    
    public static <T> List<T> getAll(String sqlString, ResultSetConverter<T> converter) throws SQLException {
        ConnectionPool cp = ConnectionPool.getInstance();
        List<T> results = new ArrayList<>();
        try(
            Connection conn = cp.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlString);
            ResultSet rs = pstmt.executeQuery();
        ) {
            while(rs.next()) {
                results.add(converter.convert(rs));
            }
        }
        return results;
    }

}
