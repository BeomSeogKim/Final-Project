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

    private final OAuthService oAuthService;

    @RequestMapping(value = "/oauth/kakao/", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public ResponseDto<?> kakaocallback(@RequestParam String code,
    HttpServletResponse response) throws JsonProcessingException {
        return oAuthService.kakaoLogin(code, response);
    }
}
