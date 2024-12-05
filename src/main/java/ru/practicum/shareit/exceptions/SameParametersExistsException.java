package ru.practicum.shareit.exceptions;

public class SameParametersExistsException extends RuntimeException {
    public SameParametersExistsException(String message) {
        super(message);
    }
}
