package ru.practicum.shareit.exceptions;

public class UserDtoException extends RuntimeException {
    public UserDtoException(String message) {
        super(message);
    }
}
