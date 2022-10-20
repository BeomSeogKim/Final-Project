package Backend.FinalProject.WebSocket.repository;


import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(ChatRoom chatRoom, LocalDateTime createdAt, Pageable pageable);
    void deleteAllByChatRoom(ChatRoom chatRoom);

    List<ChatMessage> findAllByChatRoomId(Long roomId);

    @Modifying
    @Query(value = "update ChatMessage c set c.numOfRead = c.numOfRead +1 where c.modifiedAt < :modifiedAt")
    void bulkNumOfReadPlus(@Param("modifiedAt") LocalDateTime modifiedAt);

}
