package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.IllegalItemBookingException;
import ru.practicum.shareit.exception.ItemAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationDtoException;
import ru.practicum.shareit.exception.UnavailableItemBookingException;
import ru.practicum.shareit.item.dto.CommentOutputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtended;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final RequestRepository requestRepository;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemDtoWithRequestId add(Integer userId, ItemDtoWithRequestId itemDtoWithRequestId) {
        validateItemDto(itemDtoWithRequestId);
        validateUser(userId);

        Item item = itemMapper.toItem(itemDtoWithRequestId);

        if (itemDtoWithRequestId.getRequestId() != null) {
            item.setRequest(requestRepository.findById(itemDtoWithRequestId.getRequestId()).get());
        }

        item.setOwner(userRepository.findById(userId).get());
        item = itemRepository.save(item);

        ItemDtoWithRequestId itemDtoOutput = itemMapper.toItemDtoWithRequestId(item);
        itemDtoOutput.setRequestId(itemDtoWithRequestId.getRequestId());
        return itemDtoOutput;
    }

    @Override
    public ItemDto update(Integer itemId, Integer userId, ItemDto itemDto) {
        validateItem(itemId);

        if (itemId.equals(itemDto.getId())) {
            validateUser(userId);
        }

        Item item = itemRepository.findById(itemId).get();

        if (!Objects.equals(userId, item.getOwner().getId())) {
            throw new ItemAccessException("Только владельцы могут обновлять информацию о товарах.");
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
    public ItemDtoExtended get(Integer itemId, Integer userId) {
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
            throw new NotFoundException("Объявление с id " + itemId + " не найдено.");
        }
        Item item = optionalItem.get();

        Sort sort = Sort.by("start").descending();

        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(userId, LocalDateTime.now(), sort);
        if (bookings.isEmpty()) {
            throw new UnavailableItemBookingException("Готовые объявления не найдены.");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalItemBookingException("Владельцы вещей не могут оставлять комментарии к собственному предмету.");
        }

        commentInput.setItem(item);
        commentInput.setAuthor(userRepository.findById(userId).get());

        Comment comment = commentRepository.save(commentInput);

        return commentMapper.toCommentOutputDto(comment);
    }


    private void validateItemDto(ItemDtoWithRequestId itemDtoWithRequestId) {
        if (itemDtoWithRequestId.getAvailable() == null || itemDtoWithRequestId.getName() == null || itemDtoWithRequestId.getName().isEmpty()
                || itemDtoWithRequestId.getDescription() == null) {
            throw new ValidationDtoException("Имя, описание и статус не должны быть пустыми.");
        }
        if (itemDtoWithRequestId.getRequestId() != null) {
            if (!requestRepository.existsById(itemDtoWithRequestId.getRequestId())) {
                throw new NotFoundException("Запрос с id "
                        + itemDtoWithRequestId.getRequestId() + " не существует.");
            }
        }
    }


    private void validateUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не существует.");
        }
    }

    private void validateItem(Integer itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Предмет с id " + itemId + " не существует.");
        }
    }
}
