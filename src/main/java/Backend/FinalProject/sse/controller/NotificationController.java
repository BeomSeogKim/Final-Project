package Backend.FinalProject.sse.controller;

import Backend.FinalProject.domain.UserDetailsImpl;
import Backend.FinalProject.sse.service.CommonService;
import Backend.FinalProject.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CommonService commonService;

    // MIME TYPE - text/event-stream 형태로 받아야함. EventStream의 생성은 최초 클라이언트 요청으로 발생한다. EventStream이 생성되면 서버는 원하는 시점에 n개의 EventStream에 Event 데이터를 전송할 수 있다.
    // 클라이어트로부터 오는 알림 구독 요청을 받는다.
    // 로그인한 유저는 SSE 연결
    // lAST_EVENT_ID = 이전에 받지 못한 이벤트가 존재하는 경우 [ SSE 시간 만료 혹은 종료 ]
    // 전달받은 마지막 ID 값을 넘겨 그 이후의 데이터[ 받지 못한 데이터 ]부터 받을 수 있게 한다
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpServletResponse response, @AuthenticationPrincipal UserDetailsImpl userDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
                                String lastEventId) throws Exception {

        //추가
        response.setCharacterEncoding("UTF-8");

//        Long userId = commonService.getUser().getId();

        return notificationService.subscribe(1L, lastEventId);

    }
}
