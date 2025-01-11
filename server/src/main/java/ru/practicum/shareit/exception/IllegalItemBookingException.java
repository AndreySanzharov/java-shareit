package ru.practicum.shareit.exception;

public class IllegalItemBookingException extends RuntimeException {
    public IllegalItemBookingException(String message) {
        super(message);
    }
}
