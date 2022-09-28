package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.*;
import Backend.FinalProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     * @param signupRequestDto 이미지 파일을 포함한 여러가지 데이터
     * @return
     */
    @PostMapping("/member/signup")
    public ResponseDto<String> signup(
            @ModelAttribute  SignupRequestDto signupRequestDto) {
        return memberService.createMember(signupRequestDto);
    }

    /**
     * 로그인
     * @param loginRequestDto : 로그인에 필요한 정보들을 담은 Dto
     * @param response        : Header에 Token을 담아주는 역할
     */
    @RequestMapping(value = "/member/login", method = POST, produces = "application/json; charset=utf8")
    public ResponseDto<String> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        return memberService.login(loginRequestDto, response);
    }

    /**
     * 회원정보 수정
     * @param request            : 회원 수정에 필요한 목록
     * @param request            : 회원 이미지 수정에 필요한 목록
     * @param httpServletRequest : Member 검증을 위한 param
     */
    @PutMapping("/member")
    public ResponseDto<?> editProfile(
            @ModelAttribute MemberUpdateDto request,
            HttpServletRequest httpServletRequest) {
        return memberService.updateMember(request, httpServletRequest);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/member/logout")
    public ResponseDto<?> logout(
            HttpServletRequest request) {
        return memberService.logout(request);
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/member")
    public ResponseDto<?> signout(
            HttpServletRequest request) {
        return memberService.signout(request);
    }

    /**
     * 아이디 중복검사
     */
    @PostMapping("member/id")
    public ResponseDto<?> duplicateID(@RequestBody CheckDuplicateDto userId) {
        return memberService.isPresentId(userId.getValue());
    }

    /**
     * 닉네임 중복검사
     */
    @PostMapping("member/nickname")
    public ResponseDto<?> duplicateNickname(@RequestBody CheckDuplicateDto nickname) {
        return memberService.isPresentNickname(nickname.getValue());
    }
}
