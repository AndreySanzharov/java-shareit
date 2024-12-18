package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
public class Item {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    @NotNull
    private User owner;
    private ItemRequest request;

}
