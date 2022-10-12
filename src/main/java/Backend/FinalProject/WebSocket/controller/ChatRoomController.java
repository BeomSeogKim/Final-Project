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

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseDto<?> roomInfo(@PathVariable Long roomId, HttpServletRequest request) {
        return chatRoomService.findById(roomId, request);
    }

    // 전체 채팅 내역 조회
    @GetMapping("/message")
    public ResponseDto<?> getMessageList(@RequestParam("roomId") Long roomId, @RequestParam("page") Integer pageNum ,@PageableDefault(size = 10) Pageable pageable, HttpServletRequest request) {
        return chatRoomService.getMessage(roomId, pageNum, pageable, request);
    }

    // 회원이 참여중인 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseDto<?> getRooms(HttpServletRequest request) {
        return chatRoomService.getRooms(request);
    }

    // 채팅방 참가자 조회
    @GetMapping("room/{roomId}/info")
    public ResponseDto<?> getRoomMemberInfo(@PathVariable Long roomId, HttpServletRequest request) {
        return chatRoomService.getRoomMemberInfo(roomId, request);
    }
}
