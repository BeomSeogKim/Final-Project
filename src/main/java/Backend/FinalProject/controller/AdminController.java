package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/admin/report/execute/{reportId}")
    private ResponseDto<?> executeReport(@PathVariable Long reportId, HttpServletRequest request) {
        return adminService.executeReport(reportId, request);
    }

    @PostMapping("/admin/report/withdraw/{reportId}")
    private ResponseDto<?> withdrawReport(@PathVariable Long reportId, HttpServletRequest request) {
        return adminService.withdrawReport(reportId, request);
    }




    @GetMapping("/admin/member")
    public ResponseDto<?> getReportMember(
            HttpServletRequest request) {
        return adminService.getReportMember(request);
    }

    @GetMapping("/admin/post")
    public ResponseDto<?> getReportPost(
            HttpServletRequest request) {
        return adminService.getReportPost(request);
    }

    @GetMapping("/admin/comment")
    public ResponseDto<?> getReportComment(
            HttpServletRequest request) {
        return adminService.getReportComment(request);
    }

}
