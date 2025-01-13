package ru.practicum.shareit.item.dto;

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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class ItemRequestDtoTest {

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
        ItemRequestDto dto = new ItemRequestDto("Name", "Description", true, 1L);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"name\":\"Name\"");
        assertThat(json).contains("\"description\":\"Description\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":1");
    }

    @Test
    void testDeserialization() throws Exception {
        String json = "{\"name\":\"Name\",\"description\":\"Description\"" +
                ",\"available\":true,\"requestId\":1}";

        ItemRequestDto dto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(dto.getName()).isEqualTo("Name");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(1L);
    }

    @Test
    void testValidation() {
        ItemRequestDto dto = new ItemRequestDto("Name", "Description", true, 1L);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidationFailureName() {
        ItemRequestDto dto = new ItemRequestDto("", "Description", true, 1L);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("name"));
    }

    @Test
    void testValidationFailureDescription() {
        ItemRequestDto dto = new ItemRequestDto("Name", "", true, 1L);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("description"));
    }

    @Test
    void testValidationFailureAvailable() {
        ItemRequestDto dto = new ItemRequestDto("Name", "Description", null, 1L);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("available"));
    }
}