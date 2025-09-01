package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/manager/chat")
public class ManagerChatController {

    private final ChatService chatService;

    //    관리자 채팅방 이전 메시지 조회(일반 사용자)
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getManagerChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(ChatRoomType.MANAGER, roomId);
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatMessageDtos)
                .status_code(HttpStatus.OK.value())
                .status_message("채팅내역 조회 성공")
                .build(), HttpStatus.OK);
    }

    //    관리자 채팅방 이전 메시지 조회(관리자)
    @GetMapping("/history/room/{roomId}")
    public ResponseEntity<?> getUserManagerChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chatMessageDtos = chatService.getManagerChatHistory(roomId);
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatMessageDtos)
                .status_code(HttpStatus.OK.value())
                .status_message("채팅내역 조회 성공")
                .build(), HttpStatus.OK);
    }

    //    관리자 채팅메시지 읽음처리
    @PostMapping("/{roomId}/read")
    public ResponseEntity<?> readManagerChatRoom(@PathVariable Long roomId) {
        chatService.messageRead(roomId, ChatRoomType.MANAGER);
        return new ResponseEntity<>(CommonDto.builder()
                .result("ok")
                .status_code(HttpStatus.OK.value())
                .status_message("채팅내역 읽음 처리 성공")
                .build(), HttpStatus.OK);
    }

    // 1대1 채팅 이미지 업로드
    @PostMapping("/room/{roomId}/images")
    public ResponseEntity<?> uploadFilesToManager(@PathVariable Long roomId, @RequestParam List<MultipartFile> images) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.uploadImages(roomId, ChatRoomType.MANAGER, images))
                .status_code(HttpStatus.CREATED.value())
                .status_message("1대1채팅 이미지 업로드 성공")
                .build(), HttpStatus.CREATED);
    }
}
