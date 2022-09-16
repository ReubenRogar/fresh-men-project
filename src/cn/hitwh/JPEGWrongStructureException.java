package cn.hitwh;

public class JPEGWrongStructureException extends RuntimeException {
    public JPEGWrongStructureException() {
    }

    public JPEGWrongStructureException(String message) {
        super(message);
    }

    public JPEGWrongStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public JPEGWrongStructureException(Throwable cause) {
        super(cause);
    }

    public JPEGWrongStructureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
