package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class BookItemRequestDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testSerialization() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":");
        assertThat(json).contains("\"end\":");
    }

    @Test
    void testDeserialization() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"" + LocalDateTime.now().plusDays(1) +
                "\",\"end\":\"" + LocalDateTime.now().plusDays(2) + "\"}";

        BookItemRequestDto dto = objectMapper.readValue(json, BookItemRequestDto.class);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isNotNull();
        assertThat(dto.getEnd()).isNotNull();
    }

    @Test
    void testValidation() {
        BookItemRequestDto dto = new BookItemRequestDto(1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidationFailureStart() {
        BookItemRequestDto dto = new BookItemRequestDto(1, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("start"));
    }

    @Test
    void testValidationFailureEnd() {
        BookItemRequestDto dto = new BookItemRequestDto(1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("end"));
    }
}