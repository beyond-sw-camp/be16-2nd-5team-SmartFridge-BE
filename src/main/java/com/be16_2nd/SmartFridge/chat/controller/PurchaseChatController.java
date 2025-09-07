package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/purchase/chat")
public class PurchaseChatController {

    private final ChatService chatService;

    // 공동 구매 채팅방 이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getPurchaseChatHistory(@PathVariable Long roomId,
                                                    @PageableDefault(size = 40, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(ChatRoomType.PURCHASE, roomId, pageable);
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatMessageDtos)
                .status_code(HttpStatus.OK.value())
                .status_message("채팅내역 조회 성공")
                .build(), HttpStatus.OK);
    }

    //    공동구매 채팅메시지 읽음처리
    @PostMapping("/{roomId}/read")
    public ResponseEntity<?> readPurchaseChatRoom(@PathVariable Long roomId) {
        chatService.messageRead(roomId, ChatRoomType.PURCHASE);
        return new ResponseEntity<>(CommonDto.builder()
                .result("ok")
                .status_code(HttpStatus.OK.value())
                .status_message("채팅내역 읽음 처리 성공")
                .build(), HttpStatus.OK);
    }

    // 공동구매 채팅방 개설
    @PostMapping("/create")
    public ResponseEntity<?> createPurchaseRoom(@RequestBody ChatRoomCreateDto chatRoomCreateDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.createPurchaseChatRoom(chatRoomCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("공동구매 채팅방 개설 성공")
                .build(), HttpStatus.CREATED);
    }

    //    그룹채팅목록조회
    @GetMapping("/list/{fridgeId}")
    public ResponseEntity<?> getPurchaseChatRooms(@PathVariable Long fridgeId){
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.getPurchaseChatRooms(fridgeId))
                .status_code(HttpStatus.OK.value())
                .status_message("공동구매 채팅방 목록 조회 성공")
                .build(), HttpStatus.OK);
    }

    //    공동구매채팅방참여
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinPurchaseChatRoom(@PathVariable Long roomId){
        chatService.addParticipantToPurchaseChat(roomId);
        return new ResponseEntity<>(CommonDto.builder()
                .result("ok")
                .status_code(HttpStatus.OK.value())
                .status_message("공동구매 채팅방 참여 성공")
                .build(), HttpStatus.OK);
    }

    // 공동 구매 채팅방 나가기
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> leavePurchaseChatRoom(@PathVariable Long roomId) {
        chatService.leavePurchaseChatRoom(roomId);
        return new ResponseEntity<>(CommonDto.builder()
                .result("ok")
                .status_code(HttpStatus.OK.value())
                .status_message("공동구매 채팅방 나가기 성공")
                .build(), HttpStatus.OK);
    }

    // 관리자 채팅 이미지 업로드
    @PostMapping("/room/{roomId}/images")
    public ResponseEntity<?> uploadFilesToPurchase(@PathVariable Long roomId, @RequestParam List<MultipartFile> images) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.uploadImages(roomId, ChatRoomType.PURCHASE, images))
                .status_code(HttpStatus.CREATED.value())
                .status_message("공동구매채팅 이미지 업로드 성공")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/info/{roomId}")
    public ResponseEntity<?> getRoomInfo(@PathVariable Long roomId) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.getRoomInfo(roomId))
                .status_code(HttpStatus.OK.value())
                .status_message("공동구매채팅 정보 가져오기 성공")
                .build(), HttpStatus.OK);
    }

    @GetMapping("/participants/{roomId}")
    public ResponseEntity<?> getParticipants(@PathVariable Long roomId) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.getParticipants(roomId))
                .status_code(HttpStatus.OK.value())
                .status_message("채팅방 참여자 목록 조회 성공")
                .build(), HttpStatus.OK);
    }

    @DeleteMapping("/{roomId}/complete")
    public ResponseEntity<?> completePurchase(@PathVariable Long roomId) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(chatService.completePurchase(roomId))
                .status_message("채팅방 종료 완료")
                .build(), HttpStatus.OK);
    }
}
