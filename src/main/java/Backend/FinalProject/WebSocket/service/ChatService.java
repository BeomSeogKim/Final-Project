package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatInformationDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageDto;
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

import static Backend.FinalProject.Tool.Validation.handleNull;
import static Backend.FinalProject.domain.enums.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    //== Dependency Injection ==//
    private final TokenProvider tokenProvider;
    private final SimpMessageSendingOperations messageTemplate;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMemberRepository chatMemberRepository;


    // 채팅방으로 메세지 보내기
    public ResponseDto<?> sendMessage(ChatInformationDto message, String token) {
        // 토큰으로 유저 찾기
        String nickname = tokenProvider.getUserIdByToken(token);        // 닉네임이 받아와짐
        Member member = memberRepository.findByNickname(nickname).orElse(null);

        ResponseDto<Object> checkToken = handleNull(member, CHAT_INVALID_TOKEN);
        if (checkToken != null) return checkToken;

        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId()).orElse(null);

        // 해당 채팅방에 있는 회원인지 검증
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);

        ResponseDto<Object> checkAuth = handleNull(chatMember, CHAT_NO_AUTHOR);
        if (checkAuth != null) return checkAuth;

        ResponseDto<Object> checkValidRoom = handleNull(chatRoom, CHAT_INVALID_ROOM);
        if (checkValidRoom!=null) return checkValidRoom;


        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm"));

        assert member != null;
        ChatMessageDto chatMessageDto = makeMessage(message, member, now);
        if (message.getMessage() != null) {

            // 메세지 송부
            messageTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), chatMessageDto);

            // 보낸 메세지 저장
            ChatMessage chatMessage = buildMessage(message, member, chatRoom, now);
            chatMessageRepository.save(chatMessage);

            assert chatRoom != null;
            chatRoom.updateTime(LocalDateTime.now());
        }
        return ResponseDto.success("메세지 보내기 성공");
    }

    private static ChatMessageDto makeMessage(ChatInformationDto message, Member member, String now) {
        return ChatMessageDto.builder()
                .sender(member.getNickname())
                .senderId(member.getUserId())
                .imgUrl(member.getImgUrl())
                .message(message.getMessage())
                .sendTime(now)
                .build();
    }

    private static ChatMessage buildMessage(ChatInformationDto message, Member member, ChatRoom chatRoom, String now) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(member)
                .sendTime(now)
                .message(message.getMessage())
                .build();
    }
}
