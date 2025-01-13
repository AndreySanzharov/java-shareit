package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.IllegalItemBookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnavailableItemBookingException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"db.name=test"})
public class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private BookingMapperImpl bookingMapper;

    private Item item;
    private User user;
    private User userBooker;
    private Booking booking;

    @BeforeEach
    void setup() {
        user = new User(1, "Jason", "jason@ya.ru");
        item = new Item(1, "Chair", "new Chair", true, user, null);
        userBooker = new User(2, "User", "user@ya.ru");
        booking = new Booking(1, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2), item, userBooker, BookingStatus.CANCELED);
    }

    @Test
    void add_shouldReturnBookingDtoOutput() {
        Integer bookerId = userBooker.getId();
        BookingDtoInput bookingDtoInput = new BookingDtoInput(userBooker.getId(), item.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.existsById(bookerId))
                .thenReturn(true);

        Mockito.when(itemRepository.existsById(bookingDtoInput.getItemId()))
                .thenReturn(true);

        Mockito.when(itemRepository.findById(bookingDtoInput.getItemId()))
                .thenReturn(Optional.ofNullable(item));

        Mockito.when(userRepository.findById(bookerId))
                .thenReturn(Optional.ofNullable(userBooker));

        Mockito.lenient()
                .when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDtoOutput bookingDtoOutputSaved = bookingService.add(bookerId, bookingDtoInput);

        assertNotNull(bookingDtoOutputSaved);
    }

    @Test
    void add_throwsUnavailableItemBookingException() {
        item = new Item(1, "Chair", "new Chair", false, user, null);

        Integer bookerId = userBooker.getId();
        BookingDtoInput bookingDtoInput = new BookingDtoInput(userBooker.getId(), item.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.existsById(bookerId))
                .thenReturn(true);

        Mockito.when(itemRepository.existsById(bookingDtoInput.getItemId()))
                .thenReturn(true);

        Mockito.when(itemRepository.findById(bookingDtoInput.getItemId()))
                .thenReturn(Optional.ofNullable(item));

        assertThrows(UnavailableItemBookingException.class, () -> bookingService.add(bookerId, bookingDtoInput));
    }

    @Test
    void add_throwsIllegalItemBookingException() {
        item = new Item(1, "Chair", "new chair", true, userBooker, null);

        Integer bookerId = userBooker.getId();
        BookingDtoInput bookingDtoInput = new BookingDtoInput(userBooker.getId(), item.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.existsById(bookerId))
                .thenReturn(true);

        Mockito.when(itemRepository.existsById(bookingDtoInput.getItemId()))
                .thenReturn(true);

        Mockito.when(itemRepository.findById(bookingDtoInput.getItemId()))
                .thenReturn(Optional.ofNullable(item));

        assertThrows(IllegalItemBookingException.class, () -> bookingService.add(bookerId, bookingDtoInput));
    }

    @Test
    void add_throwsObjectNotFoundException() {
        Integer bookerId = userBooker.getId();
        BookingDtoInput bookingDtoInput = new BookingDtoInput(userBooker.getId(), item.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.existsById(bookerId))
                .thenReturn(true);

        Mockito.when(itemRepository.existsById(bookingDtoInput.getItemId()))
                .thenReturn(true);

        Mockito.when(itemRepository.findById(bookingDtoInput.getItemId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.add(bookerId, bookingDtoInput));
    }

    @Test
    void get_shouldReturnBookingDtoOutput() {
        Integer bookingId = booking.getId();
        Integer userId = user.getId();

        Mockito.when(bookingRepository.existsById(bookingId))
                .thenReturn(true);

        Mockito.when(userRepository.existsById(userId))
                .thenReturn(true);

        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.ofNullable(booking));

        BookingDtoOutput bookingDtoOutputSaved = bookingService.get(bookingId, userId);

        assertNotNull(bookingDtoOutputSaved);
    }

    @Test
    void get_throwsObjectNotFoundException() {
        Integer bookingId = booking.getId();
        Integer userId = user.getId();

        Mockito.when(bookingRepository.existsById(bookingId))
                .thenReturn(true);

        Mockito.when(userRepository.existsById(userId))
                .thenReturn(true);

        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.get(bookingId, userId));
    }
}