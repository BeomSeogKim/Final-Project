package Backend.FinalProject.WebSocket.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.ChatRoomDto;
import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMemberResponseDto;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageInfoDto;
import Backend.FinalProject.WebSocket.domain.dtos.ChatMessageResponse;
import Backend.FinalProject.WebSocket.domain.dtos.ChatRequestDto;
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
import Backend.FinalProject.sse.domain.Notification;
import Backend.FinalProject.sse.repository.NotificationRepository;
import Backend.FinalProject.sse.service.NotificationService;
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
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * ??? ?????? ??????
     *
     * @param roomId             : ????????? ?????????
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional(readOnly = true)
    public ResponseDto<?> getRoomInfo(Long roomId, HttpServletRequest httpServletRequest) {

        // Token ??????
        validation.checkAccessToken(httpServletRequest);

        // TODO orElse ??????
        // ????????? ?????? ??????
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        ResponseDto<Object> checkChatRoom = handleNull(chatRoom, ErrorCode.CHATROOM_NO_CHATROOM);
        if (checkChatRoom != null) return checkChatRoom;

        assert chatRoom != null;
        return chatRoomInformation(chatRoom);
    }

    /**
     * ????????? ?????? ????????? ??????
     *
     * @param roomId             : ????????? ????????? ??????
     * @param pageNum            : ????????? ???
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> getMessageList(Long roomId, Integer pageNum, HttpServletRequest httpServletRequest) throws Exception {
        ResponseDto<?> resultOfValidation = validation.checkAccessToken(httpServletRequest);
        if (!resultOfValidation.isSuccess())
            return resultOfValidation;

        //TODO orElse ??????
        Member member = memberRepository.findById(((Member) resultOfValidation.getData()).getId()).orElse(null);
        assert member != null;

        // TODO orElse ??????
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
        handleNull(chatRoom, ErrorCode.CHATROOM_NOTFOUND);

        // TODO orElse ??????
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatRoom).orElse(null);
        ResponseDto<Object> checkMember = handleNull(chatMember, ErrorCode.CHATROOM_NO_CHATMEMBER);
        if (checkMember != null) return checkMember;

        // ?????? ????????? ?????? ??????
        readMessage(roomId, member, chatMember);

        PageRequest pageRequest = PageRequest.of(pageNum, 10, Sort.by(DESC, "createdAt"));

        //== ????????? ?????? ?????? ?????? ==//
        Page<ChatMessage> pageOfChat = chatMessageRepository.findAllByChatRoomAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(chatRoom, chatMember.getCreatedAt(), pageRequest);
        List<ChatMessage> contentOfChat = pageOfChat.getContent();
        for (ChatMessage chatMessage : contentOfChat) {
            Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
            if (validateMember == null) {           // ????????? ????????? ??????
                chatMessage.addNumOfRead();
                automatedChatService.createReadCheck(chatMember, chatMessage);
            }
        }
        List<ChatMessageResponse> chatMessageResponses = new ArrayList<>();

        getMessageInformation(contentOfChat, chatMessageResponses, member, httpServletRequest);
        return ResponseDto.success(getChatRoomInformation(pageNum, chatRoom, pageOfChat, chatMessageResponses));
    }

    /**
     * ????????? ?????? ??????
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

        ResponseDto<Object> checkActiveRoom = handleBoolean(chatList.isEmpty(), CHATROOM_NO_ACTIVEROOM);
        if (checkActiveRoom != null) return checkActiveRoom;

        List<ChatRoomListDto> chatRoomDtoList = new ArrayList<>();
        getChatRoomListInfo(chatList, chatRoomDtoList, member);
        return ResponseDto.success(chatRoomDtoList);
    }

    @Transactional
    public ResponseDto<?> readMessage(ChatRequestDto chatRequestDto, HttpServletRequest httpServletRequest) throws Exception {
        Long messageId = chatRequestDto.getMessageId();
        ResponseDto<?> validateToken = validation.checkAccessToken(httpServletRequest);
        if (!validateToken.isSuccess())
            return validateToken;

        Member member = memberRepository.findById(((Member) validateToken.getData()).getId()).orElse(null);
        assert member != null;
        ChatMessage chatMessage = chatMessageRepository.findById(messageId).orElse(null);
        if (chatMessage == null) {
            return ResponseDto.fail("NEED RELOAD", "?????? ?????? ??????????????????.");
        }
        ChatMember chatMember = chatMemberRepository.findByMemberAndChatRoom(member, chatMessage.getChatRoom()).orElse(null);
        if (chatMember == null) {
            return ResponseDto.fail("INVALID ACCESS", "????????? ???????????????.");
        }

        Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
        if (validateMember == null) {
            chatMessage.addNumOfRead();
            automatedChatService.createReadCheck(chatMember, chatMessage);
            notificationService.readNotification(notificationRepository.findByUrlAndMember(String.valueOf(chatMessage.getId()), member).getId(), httpServletRequest);
        }
//        if (chatMessage.getMember() != member) {
//            chatMessage.addNumOfRead();
//        }
        return ResponseDto.success("?????? ??????");
    }

    private void getChatRoomListInfo(List<ChatMember> chatList, List<ChatRoomListDto> chatRoomDtoList, Member member) {
        for (ChatMember chat : chatList) {
            String address;
            if (chat.getChatRoom().getPost().getDetailAddress().equals("undefined") ||
                    chat.getChatRoom().getPost().getDetailAddress().isEmpty()) {
                address = chat.getChatRoom().getPost().getAddress();
            } else {
                address = chat.getChatRoom().getPost().getAddress() + " "
                        + chat.getChatRoom().getPost().getDetailAddress();
            }
            int count = 0;
            List<ChatMessage> messageList = chatMessageRepository.findAllByChatRoomId(chat.getChatRoom().getId());
            for (ChatMessage chatMessage : messageList) {

//                Member readCheck = readCheckRepository.validateReadMember(chatMessage, chat.getMember()).orElse(null);
//                if (readCheck == null) {
//                    count++;
//                }
                if (chat.getCreatedAt().isBefore(chatMessage.getCreatedAt())) {
                    Notification notification = notificationRepository.findAllByMember(String.valueOf(chatMessage.getId()), member).orElse(null);
                    if (notification != null) {
                        if (!notification.getIsRead()) {
                            count++;
                        }
                    }
                }
            }
            chatRoomDtoList.add(
                    ChatRoomListDto.builder()
                            .roomId(chat.getChatRoom().getId())
                            .name(chat.getChatRoom().getName())
                            .numOfMember(chat.getChatRoom().getNumOfMember())
                            .numOfUnread(count)
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
            return ResponseDto.fail("NO CHAT ROOM", "?????? ???????????? ???????????? ????????????");
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

    private void getMessageInformation(List<ChatMessage> contentOfChat, List<ChatMessageResponse> chatMessageResponses, Member member, HttpServletRequest httpServletRequest) throws Exception {
        for (ChatMessage chatMessage : contentOfChat) {
            int total = chatMemberRepository.countOfAllMember(chatMessage.getChatRoom());
            Notification notification = notificationRepository.findByUrlAndMember(String.valueOf(chatMessage.getId()), member);
            if (notification != null) {
                notificationService.readNotification(notification.getId(), httpServletRequest);
            }
            chatMessageResponses.add(
                    ChatMessageResponse.builder()
                            .sender(chatMessage.getMember().getNickname())
                            .senderId(chatMessage.getMember().getUserId())
                            .message(chatMessage.getMessage())
                            .sendTime(chatMessage.getSendTime())
                            .img(chatMessage.getMember().getImgUrl())
                            .numOfUnread(total - chatMessage.getNumOfRead())
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

    @Transactional
    void readMessage(Long roomId, Member member, ChatMember chatMember) {
        List<ChatMessage> chatMessageList = chatMessageRepository.findAllByChatRoomId(roomId);
        for (ChatMessage chatMessage : chatMessageList) {
            // ????????? ??? ????????? ????????? ???????????? ??????
            Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
            if (validateMember == null) {           // ????????? ????????? ??????
                chatMessage.addNumOfRead();
                automatedChatService.createReadCheck(chatMember, chatMessage);
            }

        }
    }
}
