package Backend.FinalProject.sse.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.repository.ChatMemberRepository;
import Backend.FinalProject.WebSocket.repository.ChatMessageRepository;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.WebSocket.repository.ReadCheckRepository;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.sse.domain.Notification;
import Backend.FinalProject.sse.domain.NotificationContent;
import Backend.FinalProject.sse.domain.NotificationType;
import Backend.FinalProject.sse.domain.RelatedUrl;
import Backend.FinalProject.sse.dto.*;
import Backend.FinalProject.sse.repository.EmitterRepository;
import Backend.FinalProject.sse.repository.EmitterRepositoryImpl;
import Backend.FinalProject.sse.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static Backend.FinalProject.sse.domain.NotificationType.CHAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository = new EmitterRepositoryImpl();
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadCheckRepository readCheckRepository;
    private final NotificationRepository notificationRepository;
    private final Validation validation;

    public SseEmitter subscribe(Long memberId, String lastEventId) throws Exception {

        // emitter ??????????????? ????????? ?????? ??????
        String emitterId = memberId + "_" + System.currentTimeMillis();

        // 1?????? ??????
        Long timeout = 1000L * 60 * 60;

        // ????????? emitterId ??? ???????????? emitter ??? ??????
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(timeout));

        try {
            // emitter ??? ????????? ????????? ??? repository ?????? ??????
            emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
            emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

            //503 error ??? ???????????? ?????? ?????? ?????? ?????? ??? dummy ????????? ??????
            String eventId = memberId + "_" + System.currentTimeMillis();

            // ??? ?????? ??????????????? ???????????? ?????? ????????? ID??? ????????? ?????? ????????? ?????????.
//            sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + memberId + "]");
            sendNotification(emitter, eventId, emitterId, NotificationDto.builder().notificationContent("EventStream Created. [userId= " + memberId + "]").build());
            // ?????????????????? ??? ????????? Event ????????? ????????? ?????? ???????????? Event ????????? ??????
            if (hasLostData(lastEventId)) {
                sendLostData(lastEventId, memberId, emitterId, emitter);
            }
        } catch (Exception e) {
            throw new Exception();
        }
        return emitter;
    }

    @Async
    public void send(Member receiver, NotificationType notificationType, String notificationContent, String url) throws Exception {

        Notification notification = notificationRepository.save(createNotification(receiver, notificationType, notificationContent, url));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(receiverId);
        if (notificationType.equals(CHAT)) {
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(key, notification);
                        sendNotification(emitter, eventId, key, NotificationChatDto.create(notification));
                    }
            );
        } else {
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(key, notification);
                        sendNotification(emitter, eventId, key, NotificationDto.create(notification));
                    }
            );
        }
//        emitters.forEach(
//                (key, emitter) -> {
//                    emitterRepository.saveEventCache(key, notification);
//                    sendNotification(emitter, eventId, key, NotificationDto.create(notification));
//                }
//        );


    }

    @Transactional
    public List<NotificationDto> readNotification(Long notificationId, HttpServletRequest request) throws Exception {
        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();
        // ????????? ?????? ????????? id ??? ????????? id ??? ???????????? ?????? ????????? ?????????.
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        Notification checkNotification = notification.orElseThrow(Exception::new);
        checkNotification.read();       // ?????? ?????? 
        return findAllNotifications(member.getId());
    }

    // ??????????????? ??? ???????????? 503 ????????? ???????????? ????????? ?????????????????? ??????

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }
    // ???????????? ???????????? ????????? last - event - id??? ???????????? ??? ?????? ???????????? ????????? ????????? ???????????? ??????.

    private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    private Notification createNotification(Member receiver, NotificationType notificationType, String notificationContent, String url) throws Exception {
        return Notification.builder()
                .member(receiver)
                .notificationType(notificationType)
                .notificationContent(new NotificationContent(notificationContent))
                .url(new RelatedUrl(url))
                .isRead(false) // ?????? ????????????
                .build();
    }

    @Transactional
    public List<NotificationDto> findAllNotifications(Long userId) throws Exception {
        //try ?????? ?????? repo ?????? ????????? ???
        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        try {
            return notifications.stream()
                    .filter(notification -> !notification.getNotificationType().equals(CHAT))
                    .map(NotificationDto::create)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception();
        } finally {
            //????????????
            if (notifications.stream() != null) {
                notifications.stream().close();
            }
        }
    }
    @Transactional(readOnly = true)
    public NotificationCountDto countUnReadNotifications(Long userId) {
        //????????? ????????????????????? ->isRead(false)??? ????????? ?????? ,
        Long count = notificationRepository.countUnReadNotifications(userId, CHAT);
        return NotificationCountDto.builder()
                .count(count)
                .build();
    }

    @Transactional
    public ResponseEntity<Object> deleteAllByNotifications(Member member) {
        Long receiverId = member.getId();
        try {
            notificationRepository.deleteAllByMemberId(receiverId);
            return new ResponseEntity<>(new StatusResponseDto("?????? ?????? ???????????? ??????", true), HttpStatus.OK);
        } catch (Exception e) {
            throw new Error();
        }

    }

    @Transactional
    public ResponseEntity<Object> deleteByNotifications(Long notificationId) {
        try {
            Optional<Notification> notification = notificationRepository.findById(notificationId);
            if (notification.isPresent()) {
                notificationRepository.deleteById(notificationId);
                return new ResponseEntity<>(new StatusResponseDto("?????? ?????? ??????", true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new StatusResponseDto("???????????? ?????? ???????????????", false), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new Error();
        }
    }
    @Transactional(readOnly = true)
    public NotificationChatCountDto checkUnReadNotifications(HttpServletRequest httpServletRequest) {
        // ?????? ????????? ??????
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);
        Member member = (Member) responseDto.getData();
        List<ChatMember> chatMemberList = chatMemberRepository.findAllByMemberOrderByChatRoom(member);
        for (ChatMember chatMember : chatMemberList) {
            List<ChatRoom> chatRoomList = chatMemberRepository.findAllChatRoomById(chatMember.getId());
            for (ChatRoom chatRoom : chatRoomList) {
                List<ChatMessage> chatMessageList = chatMessageRepository.findAllChatMessageByChatRoomOrderByModifiedAtDesc(chatRoom);
                for (ChatMessage chatMessage : chatMessageList) {
                    Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
                    if (validateMember == null) {
                        return NotificationChatCountDto.builder().unreadMessage(true).build();
                    }
                }

            }
        }
        return NotificationChatCountDto.builder().unreadMessage(false).build();

//        List<ChatMember> all = chatMemberRepository.findAllByMemberOrderByChatRoom(member);
//            List<ChatMessage> chatMessageList = chatMessageRepository.findAllByMember(member);
//            for (ChatMessage chatMessage : chatMessageList) {
//                Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
//                if (validateMember == null) {
//                    return NotificationChatCountDto.builder().unreadMessage(true).build();
//                }
//            }
//        return NotificationChatCountDto.builder().unreadMessage(false).build();
    }
}
