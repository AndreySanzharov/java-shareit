package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    @PositiveOrZero
    private Integer id;
    private String name;
    @Email
    private String email;
}
