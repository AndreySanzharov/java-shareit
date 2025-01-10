package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingSearchMode;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOutput add(Integer bookerId, BookingDtoInput bookingDtoInput) {
        validateBookingDtoInput(bookingDtoInput);
        validateUser(bookerId);
        validateItem(bookingDtoInput.getItemId());

        Optional<Item> optionalItem = itemRepository.findById(bookingDtoInput.getItemId());
        if (optionalItem.isEmpty()) {
            throw new NotFoundException("Не удалось получить товар.");
        }
        Item item = optionalItem.get();

        if (!item.getAvailable()) {
            throw new UnavailableItemBookingException("Недоступные товары не могут быть забронированы.");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new IllegalItemBookingException("Владельцам товаров не разрешается самостоятельно бронировать товары.");
        }

        Booking booking = bookingMapper.toBooking(bookingDtoInput);
        booking.setBooker(userRepository.findById(bookerId).get());
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);

        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingDtoOutput(booking);
    }

    @Override
    public BookingDtoOutput setApprove(Integer bookingId, Integer userId, Boolean isApproved) {
        validateBooking(bookingId);

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new NotFoundException("Заказ с id " + bookingId + " не найден.");
        }
        Booking booking = optionalBooking.get();

        Integer itemOwnerId = booking.getItem().getOwner().getId();

        if (!Objects.equals(userId, itemOwnerId)) {
            throw new IllegalItemBookingException("Только владельцы могут менять статус.");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new UnavailableItemBookingException("Статус должен быть 'WAITING'.");
        }

        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingDtoOutput(booking);
    }

    @Override
    public BookingDtoOutput get(Integer bookingId, Integer userId) {
        validateBooking(bookingId);
        validateUser(userId);

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new NotFoundException("Бронирование с id " + bookingId + " не найдено.");
        }
        Booking booking = optionalBooking.get();

        Integer itemOwnerId = booking.getItem().getOwner().getId();
        Integer bookerId = booking.getBooker().getId();

        if (!userId.equals(itemOwnerId) && !userId.equals(bookerId)) {
            throw new NotFoundException("Только владельцам товаров и заказчикам товаров разрешено просматривать заказы.");
        }

        return bookingMapper.toBookingDtoOutput(booking);
    }

    @Override
    public List<BookingDtoOutput> getAll(String bookingSearchMode, Integer userId) {
        validateUser(userId);
        Sort sort = Sort.by("start").descending();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime currentTime = LocalDateTime.now();
        BookingSearchMode searchMode;

        try {
            searchMode = BookingSearchMode.valueOf(bookingSearchMode.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalSearchModeException("Неизвестный параметр " + bookingSearchMode);
        }
        switch (searchMode) {
            case ALL:
                return bookingRepository.findByBooker_Id(userId, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(userId, currentDateTime, currentTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBooker_IdAndEndIsBefore(userId, currentDateTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBooker_IdAndStartIsAfter(userId, currentDateTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                throw new IllegalSearchModeException("Неизвестный параметр " + bookingSearchMode);
        }
    }

    @Override
    public List<BookingDtoOutput> getAllByOwner(String bookingSearchMode, Integer userId) {
        validateUser(userId);
        Sort sort = Sort.by("start").descending();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь  с id " + userId + " не найден.");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.now();

        BookingSearchMode searchModeForOwners;

        try {
            searchModeForOwners = BookingSearchMode.valueOf(bookingSearchMode.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalSearchModeException("Неизвестный параметр " + bookingSearchMode);
        }

        switch (searchModeForOwners) {
            case ALL:
                return bookingRepository.findByItemOwnerId(userId, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, currentDateTime, dateTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, currentDateTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, currentDateTime, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort).stream()
                        .map(bookingMapper::toBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                throw new IllegalSearchModeException("Неизвестный параметр " + bookingSearchMode);
        }
    }

    private void validateBookingDtoInput(BookingDtoInput bookingDtoInput) {
        if (bookingDtoInput.getItemId() == null
                || bookingDtoInput.getStart() == null || bookingDtoInput.getEnd() == null
                || bookingDtoInput.getStart().isAfter(bookingDtoInput.getEnd())
                || bookingDtoInput.getEnd().isBefore(bookingDtoInput.getStart())
                || bookingDtoInput.getStart().equals(bookingDtoInput.getEnd())
        ) {
            throw new ValidationDtoException("Идентификатор клиента, товара, " +
                    "время начала и окончания не должны быть пустыми");
        }
    }

    private void validateUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
    }

    private void validateItem(Integer itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Предмет с id " + itemId + " не существует.");
        }
    }

    private void validateBooking(Integer bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Бронирование с id " + bookingId + " не существует.");
        }
    }
}