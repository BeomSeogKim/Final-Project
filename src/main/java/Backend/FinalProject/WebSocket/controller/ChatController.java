package Backend.FinalProject.WebSocket.controller;

import Backend.FinalProject.WebSocket.domain.dtos.ChatInformationDto;
import Backend.FinalProject.WebSocket.service.ChatService;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@RequiredArgsConstructor
@Controller
public class ChatController {
    // Dependency Injection
    private final ChatService chatService;

    /**
     * message : 주고 받기
     * @param message : 방정보 및 전달 메세지 정보
     * @param accessToken : 회원 검증을 위한 AccessToken  받기
     * @return : 해당 채팅방을 구독하는 회원들에게 메세지 송부
     */
    @MessageMapping("/chat/message")
    public ResponseDto<?> sendMessage(ChatInformationDto message, @Header("Authorization") String accessToken) {
        return chatService.sendMessage(message, accessToken);
    }
}
