package Backend.FinalProject.WebSocket.repository;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember,Long> {
    Optional<ChatMember> findByMemberAndChatRoom(Member member, ChatRoom chatRoom);

    @Query(value = "select c, c.chatRoom from ChatMember c where c.member =:member order by c.chatRoom.lastChatTime desc ")
    List<ChatMember> findAllByMemberOrderByChatRoom(@Param("member") Member member);


    List<ChatMember> findAllByChatRoomId(Long chatroomId);

    @Query(value = "select count (c) from ChatMember c where c.chatRoom =:chatRoom")
    int countOfAllMember(@Param("chatRoom") ChatRoom chatRoom);

    ChatMember findByMember(Member member);

    @Query(value = "select c.chatRoom from ChatMember c where c.id =:id")
    List<ChatRoom> findAllChatRoomById(@Param("id")Long id);
}
