package Backend.FinalProject.controller;


import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.MyActService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MyActController {

    private final MyActService myActService;

    @GetMapping("/mypage/act/host")
    public ResponseDto<?> applicantList(HttpServletRequest request) {
        return myActService.applicantList(request);
    }

    @GetMapping("/mypage/act/applicant")
    public ResponseDto<?> postList(HttpServletRequest request) {
        return myActService.postList(request);
    }
}
