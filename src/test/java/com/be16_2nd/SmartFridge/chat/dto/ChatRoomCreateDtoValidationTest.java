package com.be16_2nd.SmartFridge.chat.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomCreateDtoValidationTest {

    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 정상_입력은_통과한다_상품링크는_비워도_된다() {
        assertThat(validator.validate(valid().build())).isEmpty();
        assertThat(validator.validate(valid().productUrl("").build())).isEmpty();
        assertThat(validator.validate(valid().productUrl("https://shop.example.com/milk").build())).isEmpty();
    }

    @Test
    void 잘못된_입력은_거부된다() {
        assertThat(invalidFields(valid().fridgeId(null).build())).containsExactly("fridgeId");
        assertThat(invalidFields(valid().title(" ").build())).containsExactly("title");
        assertThat(invalidFields(valid().title("가".repeat(101)).build())).containsExactly("title");
        assertThat(invalidFields(valid().maxParticipants(null).build())).containsExactly("maxParticipants");
        assertThat(invalidFields(valid().maxParticipants(1).build())).containsExactly("maxParticipants");
        assertThat(invalidFields(valid().maxParticipants(ChatRoomCreateDto.MAX_PARTICIPANTS + 1).build())).containsExactly("maxParticipants");
        assertThat(invalidFields(valid().productUrl("javascript:alert(1)").build())).containsExactly("productUrl");
    }

    private ChatRoomCreateDto.ChatRoomCreateDtoBuilder valid() {
        return ChatRoomCreateDto.builder().fridgeId(1L).title("우유 공구").contents("같이 사요").maxParticipants(4);
    }

    private Set<String> invalidFields(ChatRoomCreateDto dto) {
        Set<ConstraintViolation<ChatRoomCreateDto>> violations = validator.validate(dto);
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }
}
