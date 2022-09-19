package Backend.FinalProject.controller;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
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
     *
     * @param request : 회원가입에 필요한 정보들을 담은 Dto
     * @param imgFile : 회원가입시 이미지 파일 업로드
     */
    @PostMapping("/member/signup")
    public ResponseDto<String> signup(
            @RequestPart SignupRequestDto request,
            @RequestPart MultipartFile imgFile) {
        return memberService.createMember(request, imgFile);
    }

    /**
     * 로그인
     *
     * @param loginRequestDto : 로그인에 필요한 정보들을 담은 Dto
     * @param response        : Header에 Token을 담아주는 역할
     */
    @PostMapping("/member/login")
    public ResponseDto<String> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        return memberService.login(loginRequestDto, response);
    }

    @PutMapping("/member")
    public ResponseDto<?> editProfile(
            @RequestPart MemberEditRequestDto request,
            @RequestPart(required = false) MultipartFile imgFile,
            HttpServletRequest httpServletRequest
    ) {
        return memberService.updateMember(request, imgFile, httpServletRequest);
    }
}
