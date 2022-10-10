package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.dto.ReportResponseDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ReportRepository reportRepository;



    private final Validation validation;

    public ResponseDto<?> getreportmember(HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        List<Report> reportMemberList = reportRepository.findByMember();
        if (reportMemberList.isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "회원을 신고한 내역이 없습니다.");
        }
        List<ReportResponseDto> reportList = new ArrayList<>();

        for(Report report : reportMemberList) {
            reportList.add(
                    ReportResponseDto.builder()
                            .id(report.getMemberId())
                            .content(report.getContent())
                            .build());
        }


        return ResponseDto.success(reportList);
    }

    public ResponseDto<?> getreportpost(HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        List<Report> reportPostList = reportRepository.findByPost();
        if (reportPostList.isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "게시글을 신고한 내역이 없습니다.");
        }
        
        List<ReportResponseDto> reportList = new ArrayList<>();

        for (Report report : reportPostList) {
            reportList.add(
                    ReportResponseDto.builder()
                            .id(report.getPostId())
                            .content(report.getContent())
                            .build());
        }


        return ResponseDto.success(reportList);
    }

    public ResponseDto<?> getreportcomment(HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        List<Report> reportCommenList = reportRepository.findBycomment();
        if (reportCommenList.isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "댓글을 신고한 내역이 없습니다.");
        }

        List<ReportResponseDto> reportList = new ArrayList<>();

        for (Report report : reportCommenList) {
            reportList.add(
                    ReportResponseDto.builder()
                            .id(report.getCommentId())
                            .content(report.getContent())
                            .build());
        }
        return ResponseDto.success(reportList);
    }
}