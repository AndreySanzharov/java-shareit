package ru.practicum.shareit.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionController {


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ItemAccessException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> hadnleItemAccessException(ItemAccessException exception) {
        return Map.of("error", exception.getMessage());
}

    @ExceptionHandler(IllegalSearchModeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> hadnleIllegalSearchModeException(IllegalSearchModeException exception) {
        return Map.of("error", exception.getMessage());
    }
}