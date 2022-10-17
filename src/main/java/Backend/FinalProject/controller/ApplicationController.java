package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ApplicationRequestDto;
import Backend.FinalProject.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 게시글 참여신청
     *
     * @param postId                : 게시글 아이디
     * @param applicationRequestDto : 지원 신청 내용
     * @param request               : Token 이 담긴 데이터
     */
    @PostMapping("/post/application/{postId}")
    public ResponseDto<?> submitApplication(
            @PathVariable Long postId,
            @RequestBody ApplicationRequestDto applicationRequestDto,
            HttpServletRequest request) {
        return applicationService.submitApplication(postId, applicationRequestDto, request);
    }

    /**
     * 게시글 참여 신청 취소
     *
     * @param postId  : 게시글 아이디
     * @param request : Token 이 담긴 데이터
     */
    @PostMapping("/post/application/cancel/{postId}")
    public ResponseDto<?> cancelApplication(
            @PathVariable Long postId,
            HttpServletRequest request) {
        return applicationService.cancelApplication(postId, request);
    }

    /**
     * 게시글 참여 수락
     *
     * @param applicationId : 지원 신청 아이디
     * @param request       : Token 이 담긴 데이터
     */
    @PostMapping("/post/application/approve/{applicationId}")
    public ResponseDto<?> approveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest request) throws Exception {
        return applicationService.approveApplication(applicationId, request);
    }

    /**
     * 게시글 참여 거절
     * @param applicationId : 지원 신청 아이디
     * @param request       : Token 이 담긴 데이터
     */
    @PostMapping("/post/application/disapprove/{applicationId}")
    public ResponseDto<?> disapproveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest request) throws Exception {
        return applicationService.disapproveApplication(applicationId, request);
    }

    /**
     * 지원자 보기
     * @param postId  : 게시글 아이디
     * @param request : Token 이 담긴 데이터
     */
    @GetMapping("/post/application/{postId}")
    public ResponseDto<?> getApplicationList(
            @PathVariable Long postId,
            HttpServletRequest request) {
        return applicationService.getApplicationList(postId, request);
    }

    /**
     * 모집 마감
     * @param postId : 게시글 아이디
     * @param request : Token 이 담긴 데이터
     */
    @PutMapping("/post/execute/{postId}")
    public ResponseDto<?> changePostStatus(
            @PathVariable Long postId,
            HttpServletRequest request
    ) {
        return applicationService.changePostStatus(postId, request);
    }
}
