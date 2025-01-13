package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(ItemRequestDto requestDto, Long ownerId) {
        return post("", ownerId, requestDto);
    }

    public ResponseEntity<Object> updateItem(ItemRequestDto requestDto, Long ownerId, Long itemId) {
        Map<String, Object> parameters = Map.of(
                "itemId", itemId
        );
        return patch("?itemId={itemId}", ownerId, parameters, requestDto);
    }

    public ResponseEntity<Object> getAllItemsByOwnerId(Long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> searchItemsByText(Long userId, String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId, CommentRequestDto requestDto) {
        return post("/" + itemId + "/comment", userId, requestDto);
    }
}