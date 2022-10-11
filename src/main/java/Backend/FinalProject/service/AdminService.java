package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.*;
import Backend.FinalProject.dto.ReportListDto;
import Backend.FinalProject.dto.response.report.ReportCommentDto;
import Backend.FinalProject.dto.response.report.ReportMemberDto;
import Backend.FinalProject.dto.response.report.ReportPostDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static Backend.FinalProject.domain.ReportStatus.UNDONE;
import static Backend.FinalProject.domain.enums.ShowStatus.SHOW;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;



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
        List<ReportMemberDto> reportList = new ArrayList<>();

        for(Report report : reportMemberList) {
            reportList.add(
                    ReportMemberDto.builder()
                            .memberId(report.getMemberId())
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
        
        List<ReportPostDto> reportList = new ArrayList<>();

        for (Report report : reportPostList) {
            reportList.add(
                    ReportPostDto.builder()
                            .postId(report.getPostId())
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
        List<Report> reportCommenList = reportRepository.findByComment();
        if (reportCommenList.isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "댓글을 신고한 내역이 없습니다.");
        }

        List<ReportCommentDto> reportList = new ArrayList<>();

        for (Report report : reportCommenList) {
            reportList.add(
                    ReportCommentDto.builder()
                            .commentId(report.getCommentId())
                            .content(report.getContent())
                            .build());
        }
        return ResponseDto.success(reportList);
    }

    public ResponseDto<?> getReportList(HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        List<Report> reportMemberList = reportRepository.findByMember();
        List<ReportMemberDto> reportMember = new ArrayList<>();

        for(Report report : reportMemberList) {
            if (report.getStatus().equals(UNDONE) && report.getShow().equals(SHOW)) {
                reportMember.add(
                        ReportMemberDto.builder()
                                .reportId(report.getId())
                                .memberId(report.getMemberId())
                                .content(report.getContent())
                                .build());
            }

        }

        List<Report> reportPostList = reportRepository.findByPost();

        List<ReportPostDto> reportPost = new ArrayList<>();

        for (Report report : reportPostList) {
            if (report.getStatus().equals(UNDONE) && report.getShow().equals(SHOW)) {
                reportPost.add(
                        ReportPostDto.builder()
                                .reportId(report.getId())
                                .postId(report.getPostId())
                                .content(report.getContent())
                                .build());
            }

        }

        List<Report> reportCommenList = reportRepository.findByComment();

        List<ReportCommentDto> reportComment = new ArrayList<>();

        for (Report report : reportCommenList) {
            if (report.getStatus().equals(UNDONE) && report.getShow().equals(SHOW)) {
                reportComment.add(
                        ReportCommentDto.builder()
                                .postId(report.getReportPostId())
                                .reportId(report.getId())
                                .commentId(report.getCommentId())
                                .content(report.getContent())
                                .build());
            }

        }
        ReportListDto reportList = ReportListDto.builder()
                .memberList(reportMember)
                .postList(reportPost)
                .commentList(reportComment)
                .build();

        return ResponseDto.success(reportList);

    }
    @Transactional
    public ResponseDto<?> executeReport(Long reportId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);
        Report report = reportRepository.findById(reportId).orElse(null);
        assert report != null;
        if (report.getMemberId() != null) {
            Member member = memberRepository.findById(report.getMemberId()).orElse(null);
            assert member != null;
            member.executeRegulation();
            memberRepository.flush();
        } else if (report.getPostId() != null) {
            Post post = postRepository.findById(report.getPostId()).orElse(null);
            assert post != null;
            post.executeRegulation();
            postRepository.flush();
            // 같은 아이디를 가진 게시글 신고글들 다 HIDE 처리
            List<Report> samePostReport = reportRepository.findAllByPostId(report.getPostId());
            samePostReport.forEach(Report::hide);
            // 같은 아이디를 가진 댓글 신고들 다 UNDONE 처리
            List<Report> samePostCommentReport = reportRepository.findAllByReportPostId(report.getPostId());
            samePostCommentReport.forEach(Report::hide);
        } else {
            Comment comment = commentRepository.findById(report.getCommentId()).orElse(null);
            assert comment != null;
            comment.executeRegulation();
            commentRepository.flush();
            // 같은 댓글을 가진 댓글 신고들 다 Undone 처리
            List<Report> sameCommentReport = reportRepository.findALlByCommentId(report.getCommentId());
            sameCommentReport.forEach(Report::hide);

        }
        report.updateStatus();
        reportRepository.flush();
        return ResponseDto.success("성공적으로 처리 되었습니다.");
    }

    @Transactional
    public ResponseDto<?> withdrawReport(Long reportId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);
        reportRepository.deleteById(reportId);
        return ResponseDto.success("성공적으로 처리 되었습니다.");
    }
}
