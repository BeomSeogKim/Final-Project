package Backend.FinalProject.controller;

import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.SignupRequestDto;
import Backend.FinalProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/member/signup")
    public ResponseDto<?> signup(
            @RequestPart SignupRequestDto request,
            @RequestPart MultipartFile imgFile) {
        return memberService.createMember(request, imgFile);
    }
}
