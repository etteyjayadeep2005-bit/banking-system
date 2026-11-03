package com.bank.bankapp.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}