package Backend.FinalProject.controller;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.CheckDuplicateDto;
import Backend.FinalProject.dto.request.LoginRequestDto;
import Backend.FinalProject.dto.request.MemberEditRequestDto;
import Backend.FinalProject.dto.request.SignupRequestDto;
import Backend.FinalProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     * @param request : 회원가입에 필요한 정보들을 담은 Dto
     * @param imgFile : 회원가입시 이미지 파일 업로드
     */
    @PostMapping("/member/signup")
    public ResponseDto<String> signup(
            @RequestPart SignupRequestDto request,
            @RequestPart(required = false) MultipartFile imgFile) {
        return memberService.createMember(request, imgFile);
    }

    /**
     * 로그인
     * @param loginRequestDto : 로그인에 필요한 정보들을 담은 Dto
     * @param response        : Header에 Token을 담아주는 역할
     */
    @PostMapping("/member/login")
    public ResponseDto<String> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        return memberService.login(loginRequestDto, response);
    }

    /**
     * 회원정보 수정
     * @param request            : 회원 수정에 필요한 목록
     * @param imgFile            : 회원 이미지 수정에 필요한 목록
     * @param httpServletRequest : Member 검증을 위한 param
     */
    @PutMapping("/member")
    public ResponseDto<?> editProfile(
            @RequestPart MemberEditRequestDto request,
            @RequestPart(required = false) MultipartFile imgFile,
            HttpServletRequest httpServletRequest
    ) {
        return memberService.updateMember(request, imgFile, httpServletRequest);
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
