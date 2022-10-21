package Backend.FinalProject.WebSocket.repository;


import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findAllByChatMember(ChatMember chatMember);
    Optional<ChatRoom> findByPostId(Long postId);

}
