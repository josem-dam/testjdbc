package edu.acceso.testjdbc.backend.dao;

import java.util.Arrays;
import java.util.List;

import edu.acceso.sqlutils.errors.DataAccessException;

public interface GenericDao<T> {
    public T get(int id) throws DataAccessException;
    public List<T> get() throws DataAccessException;
    public int insert(T entity) throws DataAccessException;
    public void insert(Iterable<T> entities) throws DataAccessException;

    default void insert(T[] entities) throws DataAccessException {
        insert(Arrays.asList(entities));
    };
    
    public boolean remove(int id) throws DataAccessException;
    public void update(T entity) throws DataAccessException;
}
