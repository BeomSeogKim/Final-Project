package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.ChatRoomDto;
import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageResponse;
import Backend.FinalProject.WebSocket.domain.dtos.ChatRoomListDto;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final Validation validation;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

//    @Transactional
//    public ChatRoom createChatRoom(String name) {
////        ChatRoom chatRoom = ChatRoom.create(name);
////
////        chatRoomRepository.save(chatRoom);
////        return chatRoom;
//        ChatRoom chatRoom = ChatRoom.builder()
//                .roomId(UUID.randomUUID().toString())
//                .name(name)
//                .build();
//        chatRoomRepository.save(chatRoom);
//        return chatRoom;
//    }

//    public ResponseDto<?> findAllRoom(HttpServletRequest request) {
//        ResponseDto<?> responseDto = validation.validateCheck(request);
//        if (!responseDto.isSuccess()) {
//            return responseDto;
//        }
//        Member member = (Member) responseDto.getData();
//        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
//        if (chatRooms.isEmpty()) {
//            return ResponseDto.fail("NO CHATROOMS", "현재 활성화된 채팅방이 존재하지 않습니다.");
//        }
//        List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
//        for (ChatRoom chatRoom : chatRooms) {
//            chatRoomDtoList.add(
//                    ChatRoomDto.builder()
//                            .id(chatRoom.getId())
//                            .roomId(chatRoom.getRoomId())
//                            .name(chatRoom.getName())
//                            .build()
//            );
//        }
//        return ResponseDto.success(chatRoomDtoList);
//    }

    public ResponseDto<?> findById(Long roomId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 방 정보 내어주자
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoom == null) {
            return ResponseDto.fail("NO CHAT ROOM", "해당 방이 존재하지 않습니다.");
        }

        return ResponseDto.success(ChatRoomDto.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getId())
                .name(chatRoom.getName())
                .build());

    }


    public ResponseDto<?> getMessage(Long roomId, HttpServletRequest request) {
        ResponseDto<?> chkResponse = validation.validateCheck(request);
        if (!chkResponse.isSuccess())
            return chkResponse;
        Member member = memberRepository.findById(((Member) chkResponse.getData()).getId()).orElse(null);
        assert member != null;

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoom == null) {
            return ResponseDto.fail("NOT FOUNT", "채팅방을 찾을 수 없습니다.");
        }

        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);
        if (chatMember == null) {
            return ResponseDto.fail("NO CHAT MEMBER", "채팅 멤버를 찾을 수 없습니다.");
        }

        List<ChatMessage> chatMessageList = chatMessageRepository.findAllByChatRoomId(chatRoom.getId());
        List<ChatMessageResponse> chatMessageResponses = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessageList) {
            chatMessageResponses.add(
                    ChatMessageResponse.builder()
                            .sender(chatMessage.getMember().getNickname())
                            .message(chatMessage.getMessage())
                            .sendTime(chatMessage.getSendTime())
                            .img(chatMessage.getMember().getImgUrl())
                            .build()
            );
        }
        return ResponseDto.success(chatMessageResponses);
    }

    public ResponseDto<?> getRooms(HttpServletRequest request) {
        ResponseDto<?> chkResponse = validation.validateCheck(request);
        if (!chkResponse.isSuccess())
            return chkResponse;
        Member member = memberRepository.findById(((Member) chkResponse.getData()).getId()).orElse(null);
        assert member != null;
        List<ChatMember> chatList = chatMemberRepository.findAllByMember(member);
        if (chatList.isEmpty() || chatList == null) {
            return ResponseDto.fail("NO CHAT ROOMS", "아직 참여중인 모임이 존재하지 않습니다.");
        }
        List<ChatRoomListDto> chatRoomDtoList = new ArrayList<>();
        for (ChatMember chat : chatList) {
            String address;
            if (chat.getChatRoom().getPost().getDetailAddress().equals("undefined") ||
                    chat.getChatRoom().getPost().getDetailAddress().isEmpty()) {
                address = chat.getChatRoom().getPost().getAddress();
            } else {
                address = chat.getChatRoom().getPost().getAddress() + " "
                        + chat.getChatRoom().getPost().getDetailAddress();
            }
            chatRoomDtoList.add(
                    ChatRoomListDto.builder()
                            .roomId(chat.getChatRoom().getId())
                            .name(chat.getChatRoom().getName())
                            .numOfMember(chat.getChatRoom().getNumOfMember())
                            .dDay(chat.getChatRoom().getPost().getDDay())
                            .address(address)
                            .build()
            );
        }
        return ResponseDto.success(chatRoomDtoList);
    }
}
