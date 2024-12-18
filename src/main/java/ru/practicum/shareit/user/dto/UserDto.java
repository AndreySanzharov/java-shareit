package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class UserDto {
    @PositiveOrZero
    private final Integer id;
    private final String name;
    @Email
    private final String email;
}
