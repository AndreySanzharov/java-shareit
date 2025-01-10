package ru.practicum.shareit.exception;

public class SameParametersExistsException extends RuntimeException {
    public SameParametersExistsException(String message) {
        super(message);
    }
}
