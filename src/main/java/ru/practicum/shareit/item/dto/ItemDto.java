package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    @NotNull
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
}
