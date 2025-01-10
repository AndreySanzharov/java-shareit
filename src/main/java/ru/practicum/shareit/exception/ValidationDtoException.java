package ru.practicum.shareit.exception;

public class ValidationDtoException extends RuntimeException {
    public ValidationDtoException(String message) {
        super(message);
    }
}
