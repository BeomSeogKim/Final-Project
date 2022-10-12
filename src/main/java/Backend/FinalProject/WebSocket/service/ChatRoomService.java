package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.ChatRoomDto;
import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMemberResponseDto;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageInfoDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageResponse;
import Backend.FinalProject.WebSocket.domain.dtos.ChatRoomListDto;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {
    // Dependency Injection
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final Validation validation;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

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


    public ResponseDto<?> getMessage(Long roomId, Integer pageNum, Pageable pageable, HttpServletRequest request) {
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

        PageRequest pageRequest = PageRequest.of(pageNum, 10, Sort.by(DESC,"createdAt"));
        List<ChatMessage> chatMessageList =  chatMessageRepository.findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(chatRoom,chatMember.getCreatedAt(),pageable);
        Page<ChatMessage> chatPage =  chatMessageRepository.findAllByChatRoomAndModifiedAtGreaterThanEqualOrderByCreatedAtDesc(chatRoom,chatMember.getModifiedAt(),pageable);
//        Page<ChatMessage> chatPage = chatMessageRepository.findByChatRoom(chatRoom, pageRequest);
        List<ChatMessageResponse> chatMessageResponses = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessageList) {
            chatMessageResponses.add(
                    ChatMessageResponse.builder()
                            .sender(chatMessage.getMember().getNickname())
                            .senderId(chatMessage.getMember().getUserId())
                            .message(chatMessage.getMessage())
                            .sendTime(chatMessage.getSendTime())
                            .img(chatMessage.getMember().getImgUrl())
                            .build()
            );
        }
        ChatMessageInfoDto chatMessageInfoDto = ChatMessageInfoDto.builder()
                .chatRoomTitle(chatRoom.getName())
                .chatMessageList(chatMessageResponses)
                .currentPage(pageNum)
                .totalPage(chatPage.getTotalPages() - 1)
                .isFirstPage(chatPage.isFirst())
                .totalMessage(chatPage.getTotalElements())
                .hasNextPage(chatPage.hasNext())
                .hasPreviousPage(chatPage.hasPrevious())
                .build();
        return ResponseDto.success(chatMessageInfoDto);
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

    public ResponseDto<?> getRoomMemberInfo(Long roomId, HttpServletRequest request) {
        ChatRoom validation = chatRoomRepository.findById(roomId).orElse(null);
        if (validation == null) {
            return ResponseDto.fail("NO CHAT ROOM", "해당 채팅방이 존재하지 않습니다");
        }
        ResponseDto<?> responseDto = this.validation.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<ChatMemberResponseDto> chatMemberInfo = new ArrayList<>();
        List<ChatMember> chatMemberList = chatMemberRepository.findAllByChatRoomId(roomId);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);

        for (ChatMember chatMember : chatMemberList) {
            boolean isLeader;
            isLeader = chatMember.getMember().getId() == (chatRoom.getPost().getMember().getId());
            chatMemberInfo.add(
                    ChatMemberResponseDto.builder()
                            .imgUrl(chatMember.getMember().getImgUrl())
                            .nickname(chatMember.getMember().getNickname())
                            .memberId(chatMember.getMember().getId())
                            .isLeader(isLeader)
                            .build()
            );
        }
        return ResponseDto.success(chatMemberInfo);
    }
}
