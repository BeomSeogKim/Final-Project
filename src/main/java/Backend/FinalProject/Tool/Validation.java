package Backend.FinalProject.Tool;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
public class Validation {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    public ResponseDto<?> checkAccessToken(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null || request.getHeader("Authorization").isEmpty()) {
            return ResponseDto.fail("NEED LOGIN", "로그인이 필요합니다.");
        }
        String token = request.getHeader("Authorization").substring(7);
        tokenProvider.validateToken(token);
        Member member = memberRepository.findByNickname(tokenProvider.getUserIdByToken(request.getHeader("Authorization"))).orElse(null);
        if (member == null) {
            return ResponseDto.fail("INVALID TOKEN", "올바르지 않은 토큰 입니다.");
        }
        return ResponseDto.success(member);
    }










//    // RefreshToken 유효성 검사
//    @Transactional
//    public Member validateMember(HttpServletRequest request) {
//        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
//            return null;
//        }
//        return tokenProvider.getMemberFromAuthentication();
//    }
//
//    // Token 유효성 검사
//    public ResponseDto<?> validateCheck(HttpServletRequest request) {
//
//        // RefreshToken 및 Authorization 유효성 검사
//        if (request.getHeader("Authorization") == null || request.getHeader("RefreshToken") == null) {
//            return ResponseDto.fail("NEED_LOGIN", "로그인이 필요합니다.");
//        }
//        Member member = validateMember(request);
//
//        // 토큰 유효성 검사
//        if (member == null) {
//            return ResponseDto.fail("INVALID TOKEN", "Token이 유효하지 않습니다.");
//        }
//        return ResponseDto.success(member);
//    }



}
