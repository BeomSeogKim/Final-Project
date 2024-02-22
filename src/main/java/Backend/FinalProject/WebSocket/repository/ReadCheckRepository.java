package Backend.FinalProject.WebSocket.repository;

import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ReadCheck;
import Backend.FinalProject.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReadCheckRepository extends JpaRepository<ReadCheck, Long> {

    List<ReadCheck> findAllByChatMessageId(Long chatMessageId);

    @Query(value = "select r.chatMember.member from ReadCheck r where r.chatMessage = :chatMessage and r.chatMember.member = :member")
    Optional<Member> validateReadMember(@Param("chatMessage") ChatMessage chatMessage, @Param("member") Member member);

    @Query(value = "select r from ReadCheck r where r.chatMessage.id = :chatMessageId")
    List<ReadCheck> findAllChatMemberByChatMessageId(@Param("chatMessageId") Long chatMessageId);


}
