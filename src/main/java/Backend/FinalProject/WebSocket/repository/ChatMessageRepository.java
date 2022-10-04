package Backend.FinalProject.WebSocket.repository;


import Backend.FinalProject.WebSocket.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
