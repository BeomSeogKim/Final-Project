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

    //== Dependency Injection ==//
    private final MyActService myActService;

    /**
     * 내가 주최한 모임 관리
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/act/host")
    public ResponseDto<?> getApplicantList(HttpServletRequest httpServletRequest) {
        return myActService.getApplicantList(httpServletRequest);
    }

    /**
     * 내가 신청한 모임 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/act/applicant")
    public ResponseDto<?> getApplicationList(HttpServletRequest httpServletRequest) {
        return myActService.getApplicationList(httpServletRequest);
    }
}
