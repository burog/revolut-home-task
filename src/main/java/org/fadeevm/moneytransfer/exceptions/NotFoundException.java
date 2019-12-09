package org.fadeevm.moneytransfer.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String id) {
        super("Resource with id #" + id);
    }
}
