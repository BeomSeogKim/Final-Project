package Backend.FinalProject.service;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class AutomatedChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(Post post) {
        ChatRoom chatRoom = ChatRoom.builder()
                .id(post.getId())
                .name(post.getTitle())
                .post(post)
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    // 채팅방 입장
    @Transactional
    public ChatMember createChatMember(Member member, ChatRoom chatRoom) {
        ChatMember chatMember = ChatMember.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();
        chatMemberRepository.save(chatMember);
        ChatRoom updateChatRoom = chatRoomRepository.findById(chatRoom.getId()).get();
        assert updateChatRoom != null;
        updateChatRoom.addMember();
        return chatMember;
    }

    // 환영 인사
    @Transactional
    public ChatMessage createChatMessage(Member member, ChatRoom chatRoom) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm"));
        ChatMessage chatMessage = ChatMessage.builder()
                .message(member.getNickname() + "님이 입장하셨습니다.")
                .member(member)
                .chatRoom(chatRoom)
                .sendTime(now)
                .build();
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }
}
