package ru.practicum.shareit.exceptions;

public class SameUsersEmailExistsException extends RuntimeException {
    public SameUsersEmailExistsException(String message) {

        super(message);
    }
}
