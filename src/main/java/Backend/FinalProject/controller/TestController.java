package Backend.FinalProject.controller;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @GetMapping("/test")
    public ResponseDto<?> createPost(
            HttpServletRequest request) {
        if (request.getHeader("Authorization") == null || request.getHeader("Authorization").isEmpty()) {
            return ResponseDto.fail("NEED LOGIN", "로그인이 필요합니다.");
        }
        String token = request.getHeader("Authorization").substring(7);
        tokenProvider.validateToken(token);
        String nickname = tokenProvider.getUserIdByToken(request.getHeader("Authorization"));        // 닉네임이 받아와짐
        log.info(nickname);
        Member member = memberRepository.findByNickname(nickname).orElse(null);
        if (member == null) {
            return ResponseDto.fail("INVALID TOKEN", "올바르지 않은 토큰 입니다.");
        }
        return ResponseDto.success(member);


//        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
