package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.application.ApplicationRequestDto;
import Backend.FinalProject.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    //== Dependency Injection
    private final ApplicationService applicationService;

    /**
     * 게시글 참여신청
     * @param postId                : 게시글 아이디
     * @param applicationRequestDto : 지원 신청 내용
     * @param httpServletRequest               : HttpServlet Request
     */
    @PostMapping("/post/application/{postId}")
    public ResponseDto<?> submitApplication(
            @PathVariable Long postId,
            @RequestBody ApplicationRequestDto applicationRequestDto,
            HttpServletRequest httpServletRequest) throws Exception {
        return applicationService.submitApplication(postId, applicationRequestDto, httpServletRequest);
    }

    /**
     * 게시글 참여 신청 취소
     * @param postId  : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/post/application/cancel/{postId}")
    public ResponseDto<?> cancelApplication(
            @PathVariable Long postId,
            HttpServletRequest httpServletRequest) {
        return applicationService.cancelApplication(postId, httpServletRequest);
    }

    /**
     * 게시글 참여 수락
     * @param applicationId : 지원 신청 아이디
     * @param httpServletRequest       : HttpServlet Request
     */
    @PostMapping("/post/application/approve/{applicationId}")
    public ResponseDto<?> approveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest httpServletRequest) throws Exception {
        return applicationService.approveApplication(applicationId, httpServletRequest);
    }

    /**
     * 게시글 참여 거절
     * @param applicationId : 지원 신청 아이디
     * @param httpServletRequest       : HttpServlet Request
     */
    @PostMapping("/post/application/disapprove/{applicationId}")
    public ResponseDto<?> disapproveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest httpServletRequest) throws Exception {
        return applicationService.disapproveApplication(applicationId, httpServletRequest);
    }

    /**
     * 지원자 보기
     * @param postId  : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/post/application/{postId}")
    public ResponseDto<?> getApplicationList(
            @PathVariable Long postId,
            HttpServletRequest httpServletRequest) {
        return applicationService.getApplicationList(postId, httpServletRequest);
    }

    /**
     * 모집 마감
     * @param postId : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @PutMapping("/post/execute/{postId}")
    public ResponseDto<?> changePostStatus(
            @PathVariable Long postId,
            HttpServletRequest httpServletRequest
    ) {
        return applicationService.changePostStatus(postId, httpServletRequest);
    }
}
