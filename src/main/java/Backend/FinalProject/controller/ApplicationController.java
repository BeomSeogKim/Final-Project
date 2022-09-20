package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ApplicationRequestDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 게시글 참여신청
     */
    @PostMapping("/post/application/{postId}")
    public ResponseDto<?> submitApplication(
            @PathVariable Long postId,
            @RequestBody ApplicationRequestDto applicationRequestDto,
            HttpServletRequest request) {
        return applicationService.submitApplication(postId, applicationRequestDto, request);
    }

    /**
     * 게시글 참여 수락
     */
    @PostMapping("/post/application/approve/{applicationId}")
    public ResponseDto<?> approveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest request) {
        return applicationService.approveApplication(applicationId, request);
    }
    /**
     * 게시글 참여 거절
     */
    @PostMapping("/post/application/disapprove/{applicationId}")
    public ResponseDto<?> disapproveApplication(
            @PathVariable Long applicationId,
            HttpServletRequest request) {
        return applicationService.disapproveApplication(applicationId, request);
    }

}
