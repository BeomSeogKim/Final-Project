package Backend.FinalProject.WebSocket;

import Backend.FinalProject.WebSocket.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findAllByMemberId(Long memberId);
    Optional<ChatRoom> findByRoomId(String roomId);
}
