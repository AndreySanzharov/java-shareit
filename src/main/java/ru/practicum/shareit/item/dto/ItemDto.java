package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ItemDto {
    @NotNull
    private final Integer id;
    private final String name;
    private final String description;
    private final Boolean available;
}
