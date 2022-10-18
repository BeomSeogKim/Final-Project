package Backend.FinalProject.WebSocket.repository;


import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(ChatRoom chatRoom, LocalDateTime createdAt, Pageable pageable);
    void deleteAllByChatRoom(ChatRoom chatRoom);
}
