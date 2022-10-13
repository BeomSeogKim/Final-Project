package Backend.FinalProject.sse.service;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.sse.domain.Notification;
import Backend.FinalProject.sse.domain.NotificationContent;
import Backend.FinalProject.sse.domain.NotificationType;
import Backend.FinalProject.sse.dto.NotificationDto;
import Backend.FinalProject.sse.repository.EmitterRepository;
import Backend.FinalProject.sse.repository.EmitterRepositoryImpl;
import Backend.FinalProject.sse.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository = new EmitterRepositoryImpl();

    private final NotificationRepository notificationRepository;

    public SseEmitter subscribe(Long memberId, String lastEventId) throws Exception {

        // emitter 하나하나에 고유한 값을 부여
        String emitterId = memberId + "_" + System.currentTimeMillis();

        // 1시간 설정
        Long timeout = 60L * 1000L * 60L;

        // 생성된 emitterId 를 기반으로 emitter를 저장
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(timeout));

        try {
            // emitter 의 시간이 만료된 후 rpository 에서 삭제
            emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
            emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

            //503 error 를 방지하기 위해 처음 연결 진행 시 dummy 데이터 전달
            String eventId = memberId + "_" + System.currentTimeMillis();

            // 수 많은 이벤트들을 구분하기 위해 이벤트 ID에 시간을 통해 구분을 해준다.
            sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + memberId + "]");

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
    public void send(Member receiver, NotificationType notificationType, String notificationContent) throws Exception {
        log.info("send 실행");
        log.info("receiver : {} , type : {} , content : {}", receiver.getNickname(), notificationType, notificationContent);

        Notification notification = notificationRepository.save(createNotification(receiver, notificationType, notificationContent));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(receiverId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationDto.create(notification));

                    log.info("emitter : {}, eventId : {} , key : {}, notify : {}", emitter, eventId, key, NotificationDto.create(notification));
                }
        );


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

    private Notification createNotification(Member receiver, NotificationType notificationType, String notificationContent) throws Exception {
        return Notification.builder()
                .member(receiver)
                .notificationType(notificationType)
                .notificationContent(new NotificationContent(notificationContent))
                .isRead(false) // 현재 읽음상태
                .build();
    }
}
