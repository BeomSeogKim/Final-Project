package Backend.FinalProject.WebSocket.repository;

import Backend.FinalProject.WebSocket.domain.ReadCheck;
import Backend.FinalProject.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReadCheckRepository extends JpaRepository<ReadCheck, Long> {

    List<ReadCheck> findAllByChatMessageId(Long chatMessageId);

    @Query(value = "select r.chatMember.member from ReadCheck r where r.chatMessage.id = :chatMessageId")
    List<Member> findAllChatMemberByChatMessageId(@Param("chatMessageId") Long chatMessageId);


}
