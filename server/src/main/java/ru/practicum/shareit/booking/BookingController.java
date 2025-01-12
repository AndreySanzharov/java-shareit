package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOutput add(@RequestHeader("X-Sharer-User-Id") @NotNull Integer userId,
                                               @Valid @RequestBody BookingDtoInput bookingDtoInput) {
        return bookingService.add(userId, bookingDtoInput);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOutput setApprove(@PathVariable @NotNull Integer bookingId,
                                                       @RequestHeader("X-Sharer-User-Id") @NotNull Integer ownerId,
                                                       @RequestParam("approved") @NotNull Boolean isApproved) {
        return bookingService.setApprove(bookingId, ownerId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOutput get(@PathVariable @NotNull Integer bookingId,
                                                @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return bookingService.get(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoOutput> getAll(@RequestParam(name = "state", defaultValue = "ALL", required = false) String searchMode,
                                                         @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return bookingService.getAll(searchMode, userId);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutput> getAllByOwner(@RequestParam(name = "state", defaultValue = "ALL", required = false) String searchMode,
                                                                @RequestHeader("X-Sharer-User-Id") @NotNull Integer userId) {
        return bookingService.getAllByOwner(searchMode, userId);
    }
}