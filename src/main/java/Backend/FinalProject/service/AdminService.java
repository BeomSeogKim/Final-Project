package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.Report;
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
import java.util.Optional;

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
            Member optionalMember = memberRepository.findById(report.getReportMemberId()).orElse(null);
            if(optionalMember==null){
                log.info("AdminService getReportList NOT FOUND");
                return ResponseDto.fail("BAD REQUEST", "올바르지 않은 접근입니다.");
            }
            reportMember.add(
                    ReportMemberDto.builder()
                            .reportId(report.getId())
                            .memberId(report.getMemberId())
                            .content(report.getContent())
                            .memberImgUrl(optionalMember.getImgUrl())
                            .reportNickname(optionalMember.getNickname())
                            .build());
        }

        List<Report> reportPostList = reportRepository.findByPost();

        List<ReportPostDto> reportPost = new ArrayList<>();

        for (Report report : reportPostList) {
            Member optionalMember = memberRepository.findById(report.getReportMemberId()).orElse(null);
            Post optionalPost = postRepository.findById(report.getPostId()).orElse(null);
            if(optionalMember==null || optionalPost==null){
                log.info("AdminService getReportList NOT FOUND");
                return ResponseDto.fail("BAD REQUEST", "올바르지 않은 접근입니다.");
            }
            reportPost.add(
                    ReportPostDto.builder()
                            .reportId(report.getId())
                            .postId(report.getPostId())
                            .content(report.getContent())
                            .postUrl(optionalPost.getImgUrl())
                            .reportNickname(optionalMember.getNickname())
                            .build());
        }

        List<Report> reportCommenList = reportRepository.findByComment();

        List<ReportCommentDto> reportComment = new ArrayList<>();

        for (Report report : reportCommenList) {
            Member optionalMember = memberRepository.findById(report.getReportMemberId()).orElse(null);
            Post optionalPost = postRepository.findById(report.getReportPostId()).orElse(null);
            Comment optionalComment = commentRepository.findById(report.getCommentId()).orElse(null);

            if(optionalMember==null || optionalPost==null || optionalComment==null){
                log.info("AdminService getReportList NOT FOUND");
                return ResponseDto.fail("BAD REQUEST", "올바르지 않은 접근입니다.");
            }
            reportComment.add(
                    ReportCommentDto.builder()
                            .postId(report.getReportPostId())
                            .reportId(report.getId())
                            .commentId(report.getCommentId())
                            .content(report.getContent())
                            .memberUrl(optionalMember.getImgUrl())
                            .reportNickname(optionalMember.getNickname())
                            .postUrl(optionalPost.getImgUrl())
                            .reportCommentContent(optionalComment.getContent())
                            .build());
        }
        ReportListDto reportList = ReportListDto.builder()
                .memberList(reportMember)
                .postList(reportPost)
                .commentList(reportComment)
                .build();

        return ResponseDto.success(reportList);

    }
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
        } else {
            Comment comment = commentRepository.findById(report.getCommentId()).orElse(null);
            assert comment != null;
            comment.executeRegulation();
            commentRepository.flush();
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
