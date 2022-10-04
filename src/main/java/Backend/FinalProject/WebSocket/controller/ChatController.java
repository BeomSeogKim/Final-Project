package Backend.FinalProject.WebSocket;

import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.domain.enums.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final Backend.FinalProject.WebSocket.ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/message")
    @Transactional
    public void message(ChatMessage message) {
        if (MessageType.ENTER.equals(message.getType()))
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");

        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);

        ChatMessage chat = ChatMessage.builder()
                .type(message.getType())
                .roomId(message.getRoomId())
                .sender(message.getSender())
                .message(message.getMessage())
                .build();
        chatMessageRepository.save(chat);
    }
}
