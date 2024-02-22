package Backend.FinalProject.api.controller.admin;

import Backend.FinalProject.api.service.admin.AdminService;
import Backend.FinalProject.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class AdminController {

    //== Dependency Injection ==//
    private final AdminService adminService;

    /**
     * 신고 내역 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/admin/report/list")
    public ResponseDto<?> getReportList(HttpServletRequest httpServletRequest) {
        return adminService.getReportList(httpServletRequest);
    }

    /**
     * 신고처리 (제재)
     * @param reportId : 신고 아이디
     * @param request : HttpServlet Request
     */
    @PostMapping("/admin/report/execute/{reportId}")
    private ResponseDto<?> executeReport(@PathVariable Long reportId, HttpServletRequest request) {
        return adminService.executeReport(reportId, request);
    }

    /**
     * 신고처리 (반려)
     * @param reportId : 신고 아이디
     * @param request : HttpServlet Request
     */
    @PostMapping("/admin/report/withdraw/{reportId}")
    private ResponseDto<?> withdrawReport(@PathVariable Long reportId, HttpServletRequest request) {
        return adminService.withdrawReport(reportId, request);
    }
}
