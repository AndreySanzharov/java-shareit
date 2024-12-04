package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ObjectDtoException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    private final Map<Integer, Item> items = new HashMap<>();
    private int totalId = 0;

    @Override
    public ItemDto create(Integer userId, ItemDto itemDto) {
        validateItemDto(itemDto);

        if (!userWithIdExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userMapper.fromUserDto(userService.get(userId)));
        item.setId(++totalId);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Integer itemId, Integer userId, ItemDto itemDto) {
        if (!userWithIdExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        Item item = items.get(itemId);

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        items.put(item.getId(), item);
        return itemMapper.toItemDto(items.get(itemId));
    }

    @Override
    public ItemDto get(Integer itemId) {
        return null;
    }

    @Override
    public List<ItemDto> getAll(Integer userId) {
        return List.of();
    }

    @Override
    public List<ItemDto> search(Integer userId, String text) {
        return List.of();
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getAvailable() == null || itemDto.getName() == null || itemDto.getName().isEmpty() || itemDto.getDescription() == null) {
            throw new ObjectDtoException("Доступность, название, описание предмета не должны быть пустыми.");
        }
    }

    private boolean userWithIdExists(Integer userId) {
        return userService.getAll().stream().anyMatch(userDto -> userDto.getId().equals(userId));
    }

    private boolean itemWithIdExists(Integer itemId) {
        return items.values().stream().anyMatch(item -> item.getId().equals(itemId));
    }

}
