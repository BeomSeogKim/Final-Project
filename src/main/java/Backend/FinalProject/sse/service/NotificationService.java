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

import static Backend.FinalProject.sse.domain.NotificationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final EmitterRepository emitterRepository = new EmitterRepositoryImpl();
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadCheckRepository readCheckRepository;
    private final NotificationRepository notificationRepository;
    private final Validation validation;

    public SseEmitter subscribe(Long memberId, String lastEventId) throws Exception {

        // emitter 하나하나에 고유한 값을 부여
        String emitterId = memberId + "_" + System.currentTimeMillis();

        // 1시간 설정
        Long timeout = 1000L * 60 * 60;

        // 생성된 emitterId 를 기반으로 emitter 를 저장
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(timeout));

        try {
            // emitter 의 시간이 만료된 후 repository 에서 삭제
            emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
            emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

            //503 error 를 방지하기 위해 처음 연결 진행 시 dummy 데이터 전달
            String eventId = memberId + "_" + System.currentTimeMillis();

            // 수 많은 이벤트들을 구분하기 위해 이벤트 ID에 시간을 통해 구분을 해준다.
//            sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + memberId + "]");
            sendNotification(emitter, eventId, emitterId, NotificationDto.builder().notificationContent("EventStream Created. [userId= " + memberId + "]").build());
            // 클라이언트가 미 수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
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
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();
        // 알림을 받은 사람의 id 와 알림의 id 를 받아와서 해당 알림을 찾는다.
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        Notification checkNotification = notification.orElseThrow(Exception::new);
        checkNotification.read();       // 읽음 처리 
        return findAllNotifications(member.getId());
    }

    // 유효시간이 다 지난다면 503 에러가 발생하기 때문에 더미데이터를 발행

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
    // 받지못한 데이터가 있다면 last - event - id를 기준으로 그 뒤의 데이터를 추출해 알림을 보내주면 된다.

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
                .isRead(false) // 현재 읽음상태
                .build();
    }

    @Transactional
    public List<NotificationDto> findAllNotifications(Long userId) throws Exception {
        //try 안에 있던 repo 조회 밖으로 뺌
        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        try {
            return notifications.stream()
                    .filter(notification -> !notification.getNotificationType().equals(CHAT))
                    .map(NotificationDto::create)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception();
        } finally {
            //추가코드
            if (notifications.stream() != null) {
                notifications.stream().close();
            }
        }
    }

    public NotificationCountDto countUnReadNotifications(Long userId) {
        //유저의 알람리스트에서 ->isRead(false)인 갯수를 측정 ,
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
            return new ResponseEntity<>(new StatusResponseDto("알림 목록 전체삭제 성공", true), HttpStatus.OK);
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
                return new ResponseEntity<>(new StatusResponseDto("알림 삭제 완료", true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new StatusResponseDto("존재하지 않는 알림입니다", false), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new Error();
        }
    }

    public NotificationChatCountDto checkUnReadNotifications(HttpServletRequest httpServletRequest) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);
        Member member = (Member) responseDto.getData();
        List<ChatMember> all = chatMemberRepository.findAllByMemberOrderByChatRoom(member);
        for (ChatMember chatMember : all) {
            List<ChatRoom> chatRoomList = chatRoomRepository.findAllByChatMember(chatMember);
            for (ChatRoom chatRoom : chatRoomList) {
                List<ChatMessage> chatMessageList = chatMessageRepository.findAllByChatRoomId(chatRoom.getId());
                for (ChatMessage chatMessage : chatMessageList) {
                    Member validateMember = readCheckRepository.validateReadMember(chatMessage, member).orElse(null);
                    if (validateMember == null) return NotificationChatCountDto.builder().unreadMessage(true).build();
                }
            }

        }
        return NotificationChatCountDto.builder().unreadMessage(false).build();
    }
}
