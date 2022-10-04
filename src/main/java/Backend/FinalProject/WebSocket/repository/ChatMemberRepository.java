package Backend.FinalProject.WebSocket.repository;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember,Long> {
    Optional<ChatMember> findByMemberAndChatRoom(Member member, ChatRoom chatRoom);
}
