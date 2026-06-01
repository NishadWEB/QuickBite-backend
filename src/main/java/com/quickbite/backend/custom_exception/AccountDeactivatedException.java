package com.quickbite.backend.custom_exception;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException(String s) {
        super(s);
    }
}
