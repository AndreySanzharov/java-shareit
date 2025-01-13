package ru.practicum.shareit.exception;

public class UnavailableItemBookingException extends RuntimeException {
    public UnavailableItemBookingException(String message) {
        super(message);
    }
}
