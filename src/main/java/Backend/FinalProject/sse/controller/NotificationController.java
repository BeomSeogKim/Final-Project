package Backend.FinalProject.sse.controller;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.sse.dto.NotificationChatCountDto;
import Backend.FinalProject.sse.dto.NotificationCountDto;
import Backend.FinalProject.sse.dto.NotificationDto;
import Backend.FinalProject.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final Validation validation;

    // MIME TYPE - text/event-stream 형태로 받아야함. EventStream의 생성은 최초 클라이언트 요청으로 발생한다. EventStream이 생성되면 서버는 원하는 시점에 n개의 EventStream에 Event 데이터를 전송할 수 있다.
    // 클라이어트로부터 오는 알림 구독 요청을 받는다.
    // 로그인한 유저는 SSE 연결
    // lAST_EVENT_ID = 이전에 받지 못한 이벤트가 존재하는 경우 [ SSE 시간 만료 혹은 종료 ]
    // 전달받은 마지막 ID 값을 넘겨 그 이후의 데이터[ 받지 못한 데이터 ]부터 받을 수 있게 한다
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpServletResponse response, HttpServletRequest request,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
                                String lastEventId) throws Exception {
        log.info("구독 시작 ");

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();
        long id = 0L;
        try {
            id = member.getId();
        } catch (Exception e) {
            log.info("INVALID ACCESS");
        }
        //추가
        response.setCharacterEncoding("UTF-8");

        return notificationService.subscribe(id, lastEventId);

    }

    //알림조회
    // TODO 채팅은 빼고 조회
    @GetMapping(value = "/notifications")
    public List<NotificationDto> findAllNotifications(HttpServletRequest request) throws Exception {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();
        return notificationService.findAllNotifications(member.getId());
    }

    // 전체목록 알림 조회에서 해당 목록 클릭 시 읽음처리
    @PostMapping("/notification/read/{notificationId}")
    public void readNotification(@PathVariable Long notificationId, HttpServletRequest request) throws Exception {
        notificationService.readNotification(notificationId, request);
    }

    //알림 조회 - 구독자가 현재 읽지않은 알림 갯수
    // TODO CHAT은 빼고 조회
    @GetMapping(value = "/notifications/count")
    public NotificationCountDto countUnReadNotifications(HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();

        return notificationService.countUnReadNotifications(member.getId());
    }

    // 채팅 관련 읽지 않은 메세지 확인
    @GetMapping("/notifications/chat")
    public NotificationChatCountDto countUnReadChatNotifications(HttpServletRequest httpServletRequest) {
        return notificationService.checkUnReadNotifications(httpServletRequest);
    }

    //알림 전체 삭제
    @DeleteMapping(value = "/notifications/delete")
    public ResponseEntity<Object> deleteNotifications(HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);
        Member member = (Member) responseDto.getData();
        return notificationService.deleteAllByNotifications(member);

    }

    //단일 알림 삭제
    @DeleteMapping(value = "/notifications/delete/{notificationId}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long notificationId) {
        return notificationService.deleteByNotifications(notificationId);
    }
}
