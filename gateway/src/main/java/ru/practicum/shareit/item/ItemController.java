package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                             @RequestBody
                                             ItemRequestDto requestDto) {
        return itemClient.createItem(requestDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                             @RequestBody @Valid ItemRequestDto requestDto, @PathVariable Long itemId) {
        return itemClient.updateItem(requestDto, ownerId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwnerId(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId) {
        return itemClient.getAllItemsByOwnerId(ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByText(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                    @RequestParam @NotBlank String text) {
        return itemClient.searchItemsByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @RequestBody @Valid CommentRequestDto requestDto) {
        return itemClient.createComment(userId, itemId, requestDto);
    }
}