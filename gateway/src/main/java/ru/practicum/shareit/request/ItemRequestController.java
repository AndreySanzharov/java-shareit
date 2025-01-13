package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId,
                                      @Valid @RequestBody RequestDto requestDto) {
        return requestClient.add(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getByUser(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return requestClient.getByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestParam(name = "from", required = false) Integer from,
                                         @RequestParam(name = "size", required = false) Integer size,
                                         @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {

        if (from != null && size != null) {
            if (size <= 0 || from < 0) {
                throw new RuntimeException("Incorrect 'from' and 'size' pagination parameter values.");
            }
        }
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> get(@PathVariable @NotNull @Positive Integer requestId,
                                      @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return requestClient.get(userId, requestId);
    }
}