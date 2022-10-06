package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatRequestDto;
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
    private final SimpMessageSendingOperations messageTemplate;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMemberRepository chatMemberRepository;



    // 채팅방으로 메세지 보내기
    public ResponseDto<?> sendMessage(ChatRequestDto message, String token) {
        // 토큰으로 유저 찾기
        String nickname = tokenProvider.getMemberIdByToken(token);        // 닉네임이 받아와짐
        Member member = memberRepository.findByNickname(nickname).orElse(null);
        if (member == null) {
            log.info("Invalid Token");
            return ResponseDto.fail("INVALID TOKEN", "유효하지 않은 토큰입니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId()).orElse(null);

        // 해당 채팅방에 있는 회원인지 검증
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);
        if (chatMember == null) {
            log.info("Invalid Member");
            return ResponseDto.fail("NO AUTHORIZATION", "해당 권한이 없습니다.");
        }

        if (chatRoom == null) {
            log.info("Invalid RoomNumber");
            return ResponseDto.fail("INVALID ROOM NUMBER", "잘못된 방 번호입니다.");
        }



        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm"));

        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .sender(member.getNickname())
                .senderId(member.getUserId())
                .imgUrl(member.getImgUrl())
                .message(message.getMessage())
                .sendTime(now)
                .build();
        if (message.getMessage() != null) {
            // 메세지 송부
            messageTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), chatMessageDto);

            // 보낸 메세지 저장
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .member(member)
                    .sendTime(now)
                    .message(message.getMessage())
                    .build();
            chatMessageRepository.save(chatMessage);

        }
        return ResponseDto.success("메세지 보내기 성공");
    }
}
