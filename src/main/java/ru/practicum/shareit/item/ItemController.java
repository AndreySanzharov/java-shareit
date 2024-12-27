package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtended;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable @Positive Integer itemId, @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.update(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable @NotNull @Positive Integer itemId,
                       @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return itemService.get(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoExtended> getAll(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return itemService.getAll(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId, @RequestParam("text") String text) {
        return itemService.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentOutputDto addComment(@PathVariable @NotNull Integer itemId,
                                       @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId,
                                       @Valid @RequestBody Comment comment) {
        return itemService.addComment(itemId, userId, comment);
    }

    @GetMapping("/{itemId}/comment")
    public ItemDtoExtended getItemWithComments(@PathVariable @NotNull Integer itemId,
                                               @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return itemService.getItemWithComments(itemId, userId);
    }
}
