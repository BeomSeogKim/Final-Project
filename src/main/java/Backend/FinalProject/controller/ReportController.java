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

    private final ReportService reportService;

    @PostMapping("/report/member/{memberId}")
    private ResponseDto<?> reportUser(@PathVariable Long memberId, @RequestBody ReportDto reportDto, HttpServletRequest request) {
        return reportService.reportUser(memberId, reportDto, request);
    }

    @PostMapping("/report/post/{postId}")
    private ResponseDto<?> reportPost(@PathVariable Long postId, @RequestBody ReportDto reportDto, HttpServletRequest request) {
        return reportService.reportPost(postId, reportDto, request);
    }

    @PostMapping("/report/comment/{commentId}")
    private ResponseDto<?> reportComment(@PathVariable Long commentId, @RequestBody ReportDto reportDto, HttpServletRequest request) {
        return reportService.reportComment(commentId, reportDto, request);
    }
}
