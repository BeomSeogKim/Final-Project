package Backend.FinalProject.WebSocket.repository;


import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatRoomId(Long roomId);

    List<ChatMessage> findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(ChatRoom chatRoom, LocalDateTime createdAt, Pageable pageable);
}
