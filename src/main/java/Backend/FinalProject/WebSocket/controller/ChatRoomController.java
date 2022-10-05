package Backend.FinalProject.WebSocket.controller;

import Backend.FinalProject.WebSocket.service.ChatRoomService;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

//    // 모든 채팅방 목록 반환
//    @GetMapping("/rooms")
//    @ResponseBody
//    public ResponseDto<?> getAllRooms(HttpServletRequest request) {
//        return chatRoomService.findAllRoom(request);
//    }

//    // 채팅방 생성
//    @PostMapping("/room")
//    public ChatRoom createRoom(@RequestParam String name) {
//        return chatRoomService.createChatRoom(name);
//    }

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseDto<?> roomInfo(@PathVariable Long roomId, HttpServletRequest request) {
        return chatRoomService.findById(roomId, request);
    }

    // 전체 채팅 내역 조회
    @GetMapping("/message")
    public ResponseDto<?> getMessageList(@RequestParam("roomId") Long roomId, @PageableDefault(size = 50) Pageable pageable, HttpServletRequest request) {
        return chatRoomService.getMessage(roomId, pageable, request);
    }

    // 회원이 참여중인 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseDto<?> getRooms(HttpServletRequest request) {
        return chatRoomService.getRooms(request);
    }
}
