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





    public ResponseDto<?> sendMessage(ChatRequestDto message, String token) {
        // 토큰으로 유저 찾기
        Long id = Long.parseLong(tokenProvider.getMemberIdByToken(token));
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            log.info("Invalid Token");
            return ResponseDto.fail("INVALID TOKEN", "유효하지 않은 토큰입니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId()).orElse(null);
        if (chatRoom == null) {
            log.info("Invalid RoomNumber");
            return ResponseDto.fail("INVALID ROOM NUMBER", "잘못된 방 번호입니다.");
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm"));

        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .sender(member.getNickname())
                .message(message.getMessage())
                .sendTime(now)
                .build();
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
        return ResponseDto.success("메세지 보내기 성공");
    }

    public ResponseDto<?> enterChatRoom(ChatRequestDto message, String token) {
        // 토큰으로 유저 찾기
        Long id = Long.parseLong(tokenProvider.getMemberIdByToken(token));
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            log.info("Invalid Token");
            return ResponseDto.fail("INVALID TOKEN", "유효하지 않은 토큰입니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId()).orElse(null);
        if (chatRoom == null) {
            log.info("Invalid RoomNumber");
            return ResponseDto.fail("INVALID ROOM NUMBER", "잘못된 방 번호입니다.");
        }
        if (chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).isPresent()) {
            return ResponseDto.fail("ALREADY EXIST", "이미 존재하는 회원입니다.");
        }

        chatMemberRepository.save(ChatMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build());

        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .sender("알림")
                .message(member.getNickname() + "님이 입장하셨습니다.")
                .sendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일 - a hh:mm ")))
                .build();
        // 메세지 송신
        messageTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), chatMessageDto);
        return ResponseDto.success("입장되었습니다.");
    }

}
