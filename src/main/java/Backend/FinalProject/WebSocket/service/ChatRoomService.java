package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.ChatRoomDto;
import Backend.FinalProject.WebSocket.domain.*;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageInfoDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageResponse;
import Backend.FinalProject.WebSocket.domain.dtos.ChatRoomListDto;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.WebSocket.repository.ReadCheckRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.enums.ErrorCode;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.service.AutomatedChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Backend.FinalProject.Tool.Validation.handleBoolean;
import static Backend.FinalProject.Tool.Validation.handleNull;
import static Backend.FinalProject.domain.enums.ErrorCode.CHATROOM_NO_ACTIVEROOM;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    //== Dependency Injection ==//
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final Validation validation;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadCheckRepository readCheckRepository;
    private final AutomatedChatService automatedChatService;

    /**
     * 방 정보 조회
     * @param roomId : 채팅방 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional(readOnly = true)
    public ResponseDto<?> getRoomInfo(Long roomId, HttpServletRequest httpServletRequest) {

        // Token 검증
        validation.checkAccessToken(httpServletRequest);

        // TODO orElse 수정
        // 채팅방 정보 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        ResponseDto<Object> checkChatRoom = handleNull(chatRoom, ErrorCode.CHATROOM_NO_CHATROOM);
        if (checkChatRoom != null) return checkChatRoom;

        assert chatRoom != null;
        return chatRoomInformation(chatRoom);
    }

    /**
     * 채팅방 관련 메세지 조회
     * @param roomId : 채팅방 아이디 조회
     * @param pageNum : 페이지 수
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> getMessageList(Long roomId, Integer pageNum, HttpServletRequest httpServletRequest) {
        ResponseDto<?> resultOfValidation = validation.checkAccessToken(httpServletRequest);
        if (!resultOfValidation.isSuccess())
            return resultOfValidation;

        //TODO orElse 수정
        Member member = memberRepository.findById(((Member) resultOfValidation.getData()).getId()).orElse(null);
        assert member != null;

        // TODO orElse 수정
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        handleNull(chatRoom,ErrorCode.CHATROOM_NOTFOUND);
//        if (chatRoom == null) {
//            return ResponseDto.fail("NOT FOUNT", "채팅방을 찾을 수 없습니다.");
//        }

        // TODO orElse 수정
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);
        handleNull(chatMember, ErrorCode.CHATROOM_NO_CHATMEMBER);
//        if (chatMember == null) {
//            return ResponseDto.fail("NO CHAT MEMBER", "채팅 멤버를 찾을 수 없습니다.");
//        }
        // 채팅 메세지 읽음 조회
        List<ChatMessage> chatMessageList = chatMessageRepository.findAllByChatRoomId(roomId);
        for (ChatMessage chatMessage : chatMessageList) {
            // 읽은 회원 인지 아닌지 검증
            List<ReadCheck> checkMemberList = readCheckRepository.findAllByChatMessageId(chatMessage.getId());
            for (ReadCheck readCheck : checkMemberList) {
                log.info(String.valueOf(readCheck.getChatMember()));
            }
            log.info(String.valueOf(member));
            if (!checkMemberList.contains(member)) {
                chatMessage.addNumOfRead();
                automatedChatService.createReadCheck(chatMember, chatMessage);
            }
        }


        PageRequest pageRequest = PageRequest.of(pageNum, 10, Sort.by(DESC,"createdAt"));

        //== 채팅방 관련 정보 조회 ==//
        Page<ChatMessage> pageOfChat =  chatMessageRepository.findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(chatRoom,chatMember.getCreatedAt(),pageRequest);
        List<ChatMessage> contentOfChat = pageOfChat.getContent();
        List<ChatMessageResponse> chatMessageResponses = new ArrayList<>();

        getMessageInformation(contentOfChat, chatMessageResponses);
        return ResponseDto.success(getChatRoomInformation(pageNum, chatRoom, pageOfChat, chatMessageResponses));
    }

    /**
     * 채팅방 목록 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional(readOnly = true)
    public ResponseDto<?> getRoomList(HttpServletRequest httpServletRequest) {
        ResponseDto<?> validateToken = validation.checkAccessToken(httpServletRequest);
        if (!validateToken.isSuccess())
            return validateToken;

        Member member = memberRepository.findById(((Member) validateToken.getData()).getId()).orElse(null);
        assert member != null;

        List<ChatMember> chatList = chatMemberRepository.findAllByMemberOrderByChatRoom(member);

        handleBoolean(chatList.isEmpty(), CHATROOM_NO_ACTIVEROOM);
//        if (chatList.isEmpty()) {
//            return ResponseDto.fail("NO CHAT ROOMS", "아직 참여중인 모임이 존재하지 않습니다.");
//        }

        List<ChatRoomListDto> chatRoomDtoList = new ArrayList<>();
        getChatRoomListInfo(chatList, chatRoomDtoList);
        return ResponseDto.success(chatRoomDtoList);
    }

    private static void getChatRoomListInfo(List<ChatMember> chatList, List<ChatRoomListDto> chatRoomDtoList) {
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
    }

    @Transactional
    public ResponseDto<?> getRoomMemberInfo(Long roomId, HttpServletRequest request) {
        ChatRoom validation = chatRoomRepository.findById(roomId).orElse(null);
        if (validation == null) {
            return ResponseDto.fail("NO CHAT ROOM", "해당 채팅방이 존재하지 않습니다");
        }
        ResponseDto<?> responseDto = this.validation.checkAccessToken(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<ChatMemberResponseDto> chatMemberInfo = new ArrayList<>();
        List<ChatMember> chatMemberList = chatMemberRepository.findAllByChatRoomId(roomId);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);

        getMemberInformation(chatMemberInfo, chatMemberList, chatRoom);
        return ResponseDto.success(chatMemberInfo);
    }

    private static ResponseDto<ChatRoomDto> chatRoomInformation(ChatRoom chatRoom) {
        return ResponseDto.success(ChatRoomDto.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getId())
                .name(chatRoom.getName())
                .build());
    }

    private static void getMessageInformation(List<ChatMessage> contentOfChat, List<ChatMessageResponse> chatMessageResponses) {
        for (ChatMessage chatMessage : contentOfChat) {
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
    }

    private static ChatMessageInfoDto getChatRoomInformation(Integer pageNum, ChatRoom chatRoom, Page<ChatMessage> pageOfChat, List<ChatMessageResponse> chatMessageResponses) {
        return ChatMessageInfoDto.builder()
                .chatRoomTitle(chatRoom.getName())
                .chatMessageList(chatMessageResponses)
                .currentPage(pageNum)
                .totalPage(pageOfChat.getTotalPages() - 1)
                .isFirstPage(pageOfChat.isFirst())
                .totalMessage(pageOfChat.getTotalElements())
                .hasNextPage(pageOfChat.hasNext())
                .hasPreviousPage(pageOfChat.hasPrevious())
                .build();
    }

    @Transactional
    void getMemberInformation(List<ChatMemberResponseDto> chatMemberInfo, List<ChatMember> chatMemberList, ChatRoom chatRoom) {
        for (ChatMember chatMember : chatMemberList) {
            boolean isLeader;
            assert chatRoom != null;
            isLeader = Objects.equals(chatMember.getMember().getId(), chatRoom.getPost().getMember().getId());
            chatMemberInfo.add(
                    ChatMemberResponseDto.builder()
                            .imgUrl(chatMember.getMember().getImgUrl())
                            .nickname(chatMember.getMember().getNickname())
                            .memberId(chatMember.getMember().getId())
                            .isLeader(isLeader)
                            .build()
            );
        }
    }
}
