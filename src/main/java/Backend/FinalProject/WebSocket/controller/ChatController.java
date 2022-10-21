package Backend.FinalProject.WebSocket.controller;

import Backend.FinalProject.WebSocket.domain.dtos.ChatInformationDto;
import Backend.FinalProject.WebSocket.service.ChatService;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;


@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatController {

    //== Dependency Injection ==//
    private final ChatService chatService;

    /**
     * WebSocket 실시간 통신 채널
     *
     * @param message     : 방정보 및 전달 메세지 정보
     * @param accessToken : 회원 검증을 위한 AccessToken  받기
     */
    @MessageMapping("/chat/message")
    public ResponseDto<?> sendMessage(ChatInformationDto message, @Header("Authorization") String accessToken) {
        return chatService.sendMessage(message, accessToken);
    }

    @PostMapping("/test")
    public ResponseDto<?> checktest() {

        log.info("test 성공");


        return ResponseDto.success("테스트 성공");
    }
}
