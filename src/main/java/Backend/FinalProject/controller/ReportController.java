package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ReportDto;
import Backend.FinalProject.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class ReportController {

    //== Dependency Injection ==//
    private final ReportService reportService;

    /**
     * 회원 신고
     * @param memberId : 신고 대상 회원 아이디
     * @param reportDto : 신고 내용
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/report/member/{memberId}")
    private ResponseDto<?> reportUser(@PathVariable Long memberId, @RequestBody ReportDto reportDto, HttpServletRequest httpServletRequest) {
        return reportService.reportUser(memberId, reportDto, httpServletRequest);
    }

    /**
     * 게시글 신고
     * @param postId : 신고 대상 게시글 아이디
     * @param reportDto : 신고 내용
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/report/post/{postId}")
    private ResponseDto<?> reportPost(@PathVariable Long postId, @RequestBody ReportDto reportDto, HttpServletRequest httpServletRequest) {
        return reportService.reportPost(postId, reportDto, httpServletRequest);
    }

    /**
     * 댓글 신고
     * @param commentId : 신고 대상 댓글 아이디
     * @param reportDto : 신고 내용
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/report/comment/{commentId}")
    private ResponseDto<?> reportComment(@PathVariable Long commentId, @RequestBody ReportDto reportDto, HttpServletRequest httpServletRequest) {
        return reportService.reportComment(commentId, reportDto, httpServletRequest);
    }
}
