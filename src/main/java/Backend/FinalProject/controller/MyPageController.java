package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.MyPageService;
import Backend.FinalProject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        return myPageService.wish(request);
    }

    @GetMapping("/mypage/leader")
    public ResponseDto<?> leader(HttpServletRequest request) {
        return myPageService.leader(request);
    }
}

