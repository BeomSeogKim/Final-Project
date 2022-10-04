package Backend.FinalProject.WebSocket;

import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Controller
@RequestMapping("chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 모든 채팅방 목록 반환
    @GetMapping("/rooms")
    @ResponseBody
    public ResponseDto<?> getAllRooms(HttpServletRequest request) {
        return chatRoomService.findAllRoom(request);
    }

    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        return chatRoomService.createChatRoom(name);
    }

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseDto<?> roomInfo(@PathVariable String roomId, HttpServletRequest request) {
        return chatRoomService.findById(roomId, request);
    }
}
