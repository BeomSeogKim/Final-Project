package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResignDto;
import Backend.FinalProject.dto.response.member.MemberPasswordUpdateDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.checkduplication.IdCheckDuplicateDto;
import Backend.FinalProject.dto.request.checkduplication.NickCheckDuplicateDto;
import Backend.FinalProject.dto.request.member.LoginRequestDto;
import Backend.FinalProject.dto.request.member.MemberUpdateDto;
import Backend.FinalProject.dto.request.member.SignupRequestDto;
import Backend.FinalProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;


@RestController
@RequiredArgsConstructor
public class MemberController {

    //== Dependency Injection ==//
    private final MemberService memberService;

    /**
     * 회원가입
     * @param signupRequestDto 회원 가입에 필요한 데이터
     */
    @PostMapping("/member/signup")
    public ResponseDto<String> signUp(
            @ModelAttribute SignupRequestDto signupRequestDto) {
        return memberService.createMember(signupRequestDto);
    }

    /**
     * 로그인
     * @param loginRequestDto : 로그인에 필요한 데이터
     * @param response        : HttpServlet Response
     */
    @PostMapping("/member/login")
    public ResponseDto<String> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) throws UnsupportedEncodingException {
        return memberService.login(loginRequestDto, response);
    }

    /**
     * 회원정보 수정
     * @param memberUpdateDto    : 회원 수정에 필요한 데이터
     * @param httpServletRequest : HttpServlet Request
     * @param httpServletResponse : HttpServlet Response
     */
    @PutMapping("/member")
    public ResponseDto<?> editProfile(
            @ModelAttribute MemberUpdateDto memberUpdateDto,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        return memberService.editProfile(memberUpdateDto, httpServletRequest, httpServletResponse);
    }

    /**
     * 비밀번호 변경
     * @param passwordUpdateDto : 비밀 번호 변경 관련한 필요 목록
     * @param httpServletRequest : Member 검증을 위한 param.
     */
    @PutMapping("/member/password")
    public ResponseDto<?> updatePassword(
            @ModelAttribute MemberPasswordUpdateDto passwordUpdateDto,
            HttpServletRequest httpServletRequest
            ) {
        return memberService.updatePassword(passwordUpdateDto, httpServletRequest);
    }

    /**
     * 로그아웃
     * @param httpServletRequest : Member 검증을 위한 Param.
     */
    @PostMapping("/member/logout")
    public ResponseDto<?> logout(
            HttpServletRequest httpServletRequest) {
        return memberService.logout(httpServletRequest);
    }

    /**
     * 회원탈퇴
     * @param httpServletRequest : Member 검증을 위한 Param.
     */
    @PutMapping("/member/signout")
    public ResponseDto<?> signOut(
            HttpServletRequest httpServletRequest) {
        return memberService.signOut(httpServletRequest);
    }

    /**
     * 아이디 중복검사
     * @param userId : 검사할 아이디
     */
    @PostMapping("/member/id")
    public ResponseDto<?> checkDuplicateId(@RequestBody IdCheckDuplicateDto userId) {
        return memberService.checkDuplicateId(userId.getIdCheck());
    }

    /**
     * 닉네임 중복검사
     * @param nickname : 검사할 닉네임
     */
    @PostMapping("/member/nickname")
    public ResponseDto<?> checkDuplicateNickname(@RequestBody NickCheckDuplicateDto nickname) {
        return memberService.checkDuplicateNickname(nickname.getNickCheck());
    }

    /**
     * AccessToken 재발급
     * @param httpServletRequest : HttpServlet Request
     * @param httpServletResponse : HttpServlet Response
     */
    @GetMapping("/member/reissue")
    public ResponseDto<?> reissueAccessToken(HttpServletRequest httpServletRequest,
                                             HttpServletResponse httpServletResponse) throws ParseException {
        return memberService.reissueAccessToken(httpServletRequest, httpServletResponse);
    }

    /**
     * 회원 재가입
     * @param resignDto : 재가입에 필요한 데이터
     */
    @PostMapping("/member/rejoin")
    public ResponseDto<?> resign(@RequestBody ResignDto resignDto) {
        return memberService.rejoin(resignDto);
    }

}
