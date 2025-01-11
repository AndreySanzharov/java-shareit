package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDtoInput;
import ru.practicum.shareit.request.dto.RequestDtoOutput;
import ru.practicum.shareit.request.dto.RequestDtoShortOutput;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestDtoShortOutput add(@RequestBody RequestDtoInput requestDtoInput,
                                     @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.add(requestDtoInput, userId);
    }

    @GetMapping
    public List<RequestDtoOutput> getByUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.getByUser(userId);
    }

    @GetMapping("/all")
    public List<RequestDtoOutput> getAll(@RequestParam(name = "from", required = false) Integer from,
                                         @RequestParam(name = "size", required = false) Integer size,
                                         @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.getAll(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public RequestDtoOutput get(@PathVariable Integer requestId,
                                @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.get(requestId, userId);
    }
}