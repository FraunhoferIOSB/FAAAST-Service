package de.fraunhofer.iosb.ilt.faaast.service.model.exception;

/**
 * Indicates that communicating with the faaast registry failed.
 */
public class RegistryException extends Exception {
    public RegistryException() {}


    public RegistryException(String message) {
        super(message);
    }


    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }


    public RegistryException(Throwable cause) {
        super(cause);
    }
}
