package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatInformationDto;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    // Dependency Injection
    private final SimpMessageSendingOperations messageTemplate;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMemberRepository chatMemberRepository;


    /**
     * 채팅 메세지 보내기
     * @param message : 방정보와 메세지 정보가 담겨 있습니다.
     * @param accessToken : 회원 검증을 위한 AccessToken 입니다.
     * @return : 메세지 보내기 및 보낸 메세지 저장.
     */
    public ResponseDto<?> sendMessage(ChatInformationDto message, String accessToken) {

        Member member = getMember(accessToken);
        if (validateMember(member) != null) return validateMember(member);

        ChatRoom chatRoom = getChatRoom(message);
        if (validateChatRoom(chatRoom) != null) return validateChatRoom(chatRoom);

        // 해당 채팅방에 있는 회원인지 검증
        ChatMember chatMember = getChatMember(member, chatRoom);
        if (validateChatMember(chatMember) != null) return validateChatMember(chatMember);

        ChatMessageDto chatMessage = makeChatMessage(message, member);
        // 메세지 송부
        messageTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), chatMessage);

        // 보낸 메세지 저장
        chatMessageRepository.save(makeSaveMessage(message, member, chatRoom));
        return ResponseDto.success("메세지 보내기 성공");
    }

    private Member getMember(String accessToken) {
        String userId = tokenProvider.getUserIdByToken(accessToken);
        Member member = memberRepository.findByUserId(userId).orElse(null);
        return member;
    }

    private static ResponseDto<Object> validateMember(Member member) {
        if (member == null) {
            log.info("ChatService sendMessage Invalid Token");
            return ResponseDto.fail("INVALID TOKEN", "유효하지 않은 토큰입니다.");
        }
        return null;
    }

    private ChatRoom getChatRoom(ChatInformationDto message) {
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId()).orElse(null);
        return chatRoom;
    }

    private static ResponseDto<Object> validateChatRoom(ChatRoom chatRoom) {
        if (chatRoom == null) {
            log.info("Invalid RoomNumber");
            return ResponseDto.fail("INVALID ROOM NUMBER", "잘못된 방 번호입니다.");
        }
        return null;
    }

    private ChatMember getChatMember(Member member, ChatRoom chatRoom) {
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);
        return chatMember;
    }

    private static ResponseDto<Object> validateChatMember(ChatMember chatMember) {
        if (chatMember == null) {
            log.info("Invalid Member");
            return ResponseDto.fail("NO AUTHORIZATION", "해당 권한이 없습니다.");
        }
        return null;
    }

    private static String getTimeNow() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm"));
        return now;
    }

    private static ChatMessageDto makeChatMessage(ChatInformationDto message, Member member) {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .sender(member.getNickname())
                .message(message.getMessage())
                .sendTime(getTimeNow())
                .build();
        return chatMessageDto;
    }

    private static ChatMessage makeSaveMessage(ChatInformationDto message, Member member, ChatRoom chatRoom) {
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(member)
                .sendTime(getTimeNow())
                .message(message.getMessage())
                .build();
        return chatMessage;
    }
}
