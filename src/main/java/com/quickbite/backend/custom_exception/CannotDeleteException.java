package com.quickbite.backend.custom_exception;

public class CannotDeleteException extends RuntimeException {
    public CannotDeleteException(String s) {
        super(s);
    }
}
