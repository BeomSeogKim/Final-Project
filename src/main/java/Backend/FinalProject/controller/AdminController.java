package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/admin/report/list")
    public ResponseDto<?> getReportList(HttpServletRequest request) {
        return adminService.getReportList(request);
    }
    @GetMapping("/admin/member")
    public ResponseDto<?> getReportMember(
            HttpServletRequest request) {
        return adminService.getreportmember(request);
    }

    @GetMapping("/admin/post")
    public ResponseDto<?> getReportPost(
            HttpServletRequest request) {
        return adminService.getreportpost(request);
    }

    @GetMapping("/admin/comment")
    public ResponseDto<?> getReportComment(
            HttpServletRequest request) {
        return adminService.getreportcomment(request);
    }

}
