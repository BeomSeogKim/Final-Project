package Backend.FinalProject.WebSocket.controller;

import Backend.FinalProject.WebSocket.service.ChatRoomService;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("chat")
public class ChatRoomController {

    //== Dependency Injection ==//
    private final ChatRoomService chatRoomService;

    /**
     * 특정 채팅방 조회
     * @param roomId : 채팅방 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseDto<?> getRoomInfo(@PathVariable Long roomId,
                                      HttpServletRequest httpServletRequest) {
        return chatRoomService.getRoomInfo(roomId, httpServletRequest);
    }

    /**
     * 전체 채팅 내역 조회
     * @param roomId : 채팅방 아이디
     * @param pageNum : 페이지 번호
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/message")
    public ResponseDto<?> getMessageList(@RequestParam("roomId") Long roomId,
                                         @RequestParam("page") Integer pageNum,
                                         HttpServletRequest httpServletRequest) {
        return chatRoomService.getMessageList(roomId, pageNum, httpServletRequest);
    }

    /**
     * 회원이 참여중인 채팅방 목록 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/rooms")
    public ResponseDto<?> getRoomList(HttpServletRequest httpServletRequest) {
        return chatRoomService.getRoomList(httpServletRequest);
    }

    /**
     * 채팅방 참가자 조회
     * @param roomId : 채팅방 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("room/{roomId}/info")
    public ResponseDto<?> getRoomMemberInfo(@PathVariable Long roomId,
                                            HttpServletRequest httpServletRequest) {
        return chatRoomService.getRoomMemberInfo(roomId, httpServletRequest);
    }

    @PostMapping("/chat/read")
    public ResponseDto<?> checktest(Long messageId, HttpServletRequest httpServletRequest) {

        return chatRoomService.readMessage(messageId, httpServletRequest);

    }
}
