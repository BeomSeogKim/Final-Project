package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/mypage/participation")
    public ResponseDto<?> participation(HttpServletRequest request) {
        return myPageService.participation(request);
    }


    @GetMapping("/mypage/application")
    public ResponseDto<?> application(HttpServletRequest request) {
        return myPageService.application(request);
    }

    @GetMapping("/mypage/wish")
    public ResponseDto<?> wish(HttpServletRequest request) {
        return myPageService.addWish(request);
    }

    @GetMapping("/mypage/leader")
    public ResponseDto<?> leader(HttpServletRequest request) {
        return myPageService.leader(request);
    }

    @GetMapping("/mypage/info")
    public ResponseDto<?> getInfo(HttpServletRequest request) {
        return myPageService.getInfo(request);
    }
    // 회원 정보 상세조회
    @GetMapping("/mypage/{memberId}")
    public ResponseDto<?> getMemberMypage(@PathVariable Long memberId,
                                        HttpServletRequest request) {
        return myPageService.getMemberMypage(memberId,request);
    }
    // 해당 회원이 작성한 게시글 조회
    @GetMapping("/mypage/leader/{memberId}")
    public ResponseDto<?> getMemberPost(@PathVariable Long memberId,
                                          HttpServletRequest request) {
        return myPageService.getMemberPost(memberId,request);
    }


}

