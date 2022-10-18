package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.OAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    //== Dependency Injection ==//
    private final OAuthService oAuthService;

    /**
     * OAUTH 카카오 로그인 관련 코드
     * @param code : 카카오 서버에서 받아오는 인증 코드
     * @param httpServletResponse : HttpServlet Response
     */
    @RequestMapping(value = "/oauth/kakao", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public ResponseDto<?> kakaoCallback(@RequestParam String code,
    HttpServletResponse httpServletResponse) throws JsonProcessingException {
        return oAuthService.kakaoLogin(code, httpServletResponse);
    }
}
