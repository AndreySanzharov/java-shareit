package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;


    @Override
    public ItemDto create(Integer userId, ItemDto itemDto) {
        validateItemDto(itemDto);

        if (!userWithIdExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userMapper.fromUserDto(userService.get(userId)));
        item = itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Integer itemId, Integer userId, ItemDto itemDto) {
        validateItem(itemId);

        if (itemId.equals(itemDto.getId())) {
            validateUser(userId);
        }

        Item item = itemRepository.findById(itemId).get();

        if (!Objects.equals(userId, item.getOwner().getId())) {
            throw new ItemAccessException("Только владельцам вещей разрешено обновлять их.");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        item = itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto get(Integer itemId, Integer userId) {
        validateItem(itemId);

        Sort sort = Sort.by("start").descending();

        Item item = itemRepository.findById(itemId).get();
        ItemDto itemDto = itemMapper.toItemDto(item);

        List<CommentOutputDto> itemComments = commentRepository.findByItemId(itemId).stream()
                .map(commentMapper::toCommentOutputDto).collect(Collectors.toList());

        ItemDtoExtended itemDtoExtended = new ItemDtoExtended(itemDto, itemComments);

        if (Objects.equals(userId, item.getOwner().getId())) {
            List<Booking> last = bookingRepository.findByItemIdAndStartIsBeforeAndStatusNot(itemId, LocalDateTime.now(), BookingStatus.REJECTED, sort);
            if (!last.isEmpty()) {
                Booking lastBooking = last.get(0);
                itemDtoExtended.setLastBooking(bookingMapper.toBookingDtoShortOutput(lastBooking));
            }

            List<Booking> next = bookingRepository.findByItemIdAndStartIsAfterAndStatusNot(itemId, LocalDateTime.now(), BookingStatus.REJECTED, sort.ascending());
            if (!next.isEmpty()) {
                Booking nextBooking = next.get(0);
                itemDtoExtended.setNextBooking(bookingMapper.toBookingDtoShortOutput(nextBooking));
            }
        }
        return itemDtoExtended;
    }

    @Override
    public List<ItemDtoExtended> getAll(Integer userId) {
        Sort sort = Sort.by("start").descending();

        List<ItemDtoExtended> userItems = itemRepository.findByOwnerId(userId).stream()
                .map(itemMapper::toItemDto)
                .map(itemDto -> new ItemDtoExtended(itemDto, null))
                .map(item -> {
                    List<Booking> last = bookingRepository.findByItemIdAndStartIsBeforeAndStatusNot(item.getId(), LocalDateTime.now(), BookingStatus.REJECTED, sort);
                    if (!last.isEmpty()) {
                        Booking lastBooking = last.get(0);
                        item.setLastBooking(bookingMapper.toBookingDtoShortOutput(lastBooking));
                    }

                    List<Booking> next = bookingRepository.findByItemIdAndStartIsAfterAndStatusNot(item.getId(), LocalDateTime.now(), BookingStatus.REJECTED, sort.ascending());
                    if (!next.isEmpty()) {
                        Booking nextBooking = next.get(0);
                        item.setNextBooking(bookingMapper.toBookingDtoShortOutput(nextBooking));
                    }
                    return item;
                }).collect(Collectors.toList());

        return userItems;
    }

    @Override
    public List<ItemDto> search(Integer userId, String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        String searchQuery = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchQuery)
                        || item.getDescription().toLowerCase().contains(searchQuery))
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentOutputDto addComment(Integer itemId, Integer userId, Comment commentInput) {
        validateUser(userId);
        validateItem(itemId);

        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if ((optionalItem.isEmpty())) {
            throw new NotFoundException("Брониргование с id  " + itemId + " не существует");
        }
        Item item = optionalItem.get();

        Sort sort = Sort.by("start").descending();

        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(userId, LocalDateTime.now(), sort);
        if (bookings.isEmpty()) {
            throw new UnavailableItemBookingException("Готовые заказы не найдены.");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalItemBookingException("Владельцам товаров запрещено комментировать бронирование своих товаров.");
        }

        commentInput.setItem(item);
        commentInput.setAuthor(userRepository.findById(userId).get());

        Comment comment = commentRepository.save(commentInput);

        return commentMapper.toCommentOutputDto(comment);
    }

    @Override
    public ItemDtoExtended getItemWithComments(Integer itemId, Integer userId) {
        validateUser(userId);
        validateItem(itemId);

        ItemDto itemDto = get(itemId, userId);

        List<CommentOutputDto> itemComments = commentRepository.findByItemId(itemId).stream()
                .map(commentMapper::toCommentOutputDto).collect(Collectors.toList());

        return new ItemDtoExtended(itemDto, itemComments);
    }


    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getAvailable() == null || itemDto.getName() == null || itemDto.getName().isEmpty() || itemDto.getDescription() == null) {
            throw new ObjectDtoException("Доступность, название, описание предмета не должны быть пустыми.");
        }
    }

    private boolean userWithIdExists(Integer userId) {
        return userService.getAll().stream().anyMatch(userDto -> userDto.getId().equals(userId));
    }

    private void validateUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с id " + userId + " не существует");
        }
    }

    private void validateItem(Integer itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Предмет с id " + itemId + " не существует");
        }
    }
}
