package Backend.FinalProject.WebSocket.controller;

import Backend.FinalProject.WebSocket.domain.dtos.ChatRequestDto;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.service.ChatService;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;


    @MessageMapping("/chat/message")
    public ResponseDto<?> message(ChatRequestDto message, @Header("Authorization") String token) {
        return chatService.sendMessage(message, token);
    }
}
