package edu.acceso.testjdbc.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public interface GenericDao<T> {
    public T get(int id) throws SQLException;
    public List<T> get() throws SQLException;
    public int insert(T entity) throws SQLException;
    public void insert(Iterable<T> entities) throws SQLException;

    default void insert(T[] entities) throws SQLException {
        insert(Arrays.asList(entities));
    };
    
    public boolean remove(int id) throws SQLException;
    public void update(T entity) throws SQLException;
}
