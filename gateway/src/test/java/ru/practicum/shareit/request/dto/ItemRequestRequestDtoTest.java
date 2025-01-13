package ru.practicum.shareit.request.dto;

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
class ItemRequestRequestDtoTest {

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
        RequestDto dto = new RequestDto("Description");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"description\":\"Description\"");
    }

    @Test
    void testDeserialization() throws Exception {
        String json = "{\"description\":\"Description\"}";

        RequestDto dto = objectMapper.readValue(json, RequestDto.class);

        assertThat(dto.getDescription()).isEqualTo("Description");
    }

    @Test
    void testValidation() {
        RequestDto dto = new RequestDto("Description");

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testValidationFailureDescription() {
        RequestDto dto = new RequestDto("");

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> violation.getPropertyPath()
                .toString().equals("description"));
    }
}