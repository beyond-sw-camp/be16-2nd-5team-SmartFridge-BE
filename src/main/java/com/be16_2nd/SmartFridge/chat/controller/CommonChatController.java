package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.dto.MyChatListResDto;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class CommonChatController {

    private final ChatService chatService;

    // 내 채팅 목록
    @GetMapping("/my/rooms/{fridgeId}")
    public ResponseEntity<?> getMyChatRooms(@PathVariable Long fridgeId) {
        List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms(fridgeId);
        return new ResponseEntity<>(CommonDto.builder()
                .result(myChatListResDtos)
                .status_code(HttpStatus.OK.value())
                .status_message("내 채팅 목록 조회 성공")
                .build(), HttpStatus.OK);
    }
}
