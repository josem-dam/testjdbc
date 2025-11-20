package edu.acceso.testjdbc;

public class DataAccessRuntimeException extends RuntimeException {

    public DataAccessRuntimeException(String message) {
        super(message);
    }

    public DataAccessRuntimeException(Throwable e) {
        super(e);
    }

    public DataAccessRuntimeException(String message, Throwable e) {
        super(message, e);
    }
}
