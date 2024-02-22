package Backend.FinalProject.api.service.admin;

import Backend.FinalProject.api.service.report.response.ReportCommentDto;
import Backend.FinalProject.api.service.report.response.ReportListDto;
import Backend.FinalProject.api.service.report.response.ReportMemberDto;
import Backend.FinalProject.api.service.report.response.ReportPostDto;
import Backend.FinalProject.common.Tool.Validation;
import Backend.FinalProject.common.dto.ResponseDto;
import Backend.FinalProject.domain.comment.Comment;
import Backend.FinalProject.domain.comment.CommentRepository;
import Backend.FinalProject.domain.member.Member;
import Backend.FinalProject.domain.member.MemberRepository;
import Backend.FinalProject.domain.post.Post;
import Backend.FinalProject.domain.post.PostRepository;
import Backend.FinalProject.domain.report.Report;
import Backend.FinalProject.domain.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static Backend.FinalProject.common.Tool.Validation.handleBoolean;
import static Backend.FinalProject.common.Tool.Validation.handleNull;
import static Backend.FinalProject.domain.enums.ErrorCode.ADMIN_BAD_REQUEST;
import static Backend.FinalProject.domain.enums.ReportStatus.UNDONE;
import static Backend.FinalProject.domain.enums.ShowStatus.SHOW;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    //== Dependency Injection ==//
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final Validation validation;

    /**
     * 신고 내역 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional(readOnly = true)
    public ResponseDto<?> getReportList(HttpServletRequest httpServletRequest) {
        validation.checkAccessToken(httpServletRequest);

        List<Report> reportMemberList = reportRepository.findByMember(UNDONE, SHOW);
        List<ReportMemberDto> reportMember = new ArrayList<>();
        for (Report report : reportMemberList) {
            Member optionalMember = memberRepository.findById(report.getMemberId()).orElse(null);
            handleNull(optionalMember, ADMIN_BAD_REQUEST);
            addReportMember(reportMember, report, optionalMember);
        }

        List<Report> reportPostList = reportRepository.findByPost(UNDONE, SHOW);
        List<ReportPostDto> reportPost = new ArrayList<>();
        for (Report report : reportPostList) {
            Member optionalMember = memberRepository.findById(report.getReportMemberId()).orElse(null);
            Post optionalPost = postRepository.findById(report.getPostId()).orElse(null);
            handleBoolean(optionalMember == null || optionalPost == null, ADMIN_BAD_REQUEST);
            addReportPost(reportPost, report, optionalMember, optionalPost);
        }

        List<Report> reportCommentList = reportRepository.findByComment(UNDONE, SHOW);
        List<ReportCommentDto> reportComment = new ArrayList<>();
        for (Report report : reportCommentList) {
            Comment comment = commentRepository.findById(report.getCommentId()).orElse(null);
            handleNull(comment, ADMIN_BAD_REQUEST);

            Post post = postRepository.findById(report.getReportPostId()).orElse(null);
            handleNull(post, ADMIN_BAD_REQUEST);

            Member member = memberRepository.findById(comment.getMember().getId()).orElse(null);
            handleNull(member, ADMIN_BAD_REQUEST);

            addReportComment(reportComment, report, comment, post, member);
        }

        ReportListDto reportList = makeReportList(reportMember, reportPost, reportComment);

        return ResponseDto.success(reportList);

    }

    /**
     * 제재 처리
     * @param reportId : 신고 번호
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> executeReport(Long reportId, HttpServletRequest httpServletRequest) {
        validation.checkAccessToken(httpServletRequest);
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
            reportRepository.findAllByPostId(report.getPostId()).forEach(Report::hide);
            // 같은 아이디를 가진 댓글 신고들 다 UNDONE 처리
            reportRepository.findAllByReportPostId(report.getPostId()).forEach(Report::hide);
        } else {
            Comment comment = commentRepository.findById(report.getCommentId()).orElse(null);
            assert comment != null;
            comment.executeRegulation();
            commentRepository.flush();
            // 같은 댓글을 가진 댓글 신고들 다 Undone 처리
            reportRepository.findALlByCommentId(report.getCommentId()).forEach(Report::hide);

        }
        report.updateStatus();
        reportRepository.flush();
        return ResponseDto.success("성공적으로 처리 되었습니다.");
    }

    /**
     * 반려 처리
     * @param reportId : 신고 번호
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> withdrawReport(Long reportId, HttpServletRequest httpServletRequest) {
        validation.checkAccessToken(httpServletRequest);
        reportRepository.deleteById(reportId);
        return ResponseDto.success("성공적으로 처리 되었습니다.");
    }

    private static void addReportMember(List<ReportMemberDto> reportMember, Report report, Member optionalMember) {
        reportMember.add(
                ReportMemberDto.builder()
                        .reportId(report.getId())
                        .memberId(report.getMemberId())
                        .content(report.getContent())
                        .memberImgUrl(optionalMember.getImgUrl())
                        .reportNickname(optionalMember.getNickname())
                        .build());
    }

    private static void addReportPost(List<ReportPostDto> reportPost, Report report, Member optionalMember, Post optionalPost) {
        reportPost.add(
                ReportPostDto.builder()
                        .reportId(report.getId())
                        .postId(report.getPostId())
                        .content(report.getContent())
                        .postUrl(optionalPost.getImgUrl())
                        .reportNickname(optionalMember.getNickname())
                        .build());
    }

    private static void addReportComment(List<ReportCommentDto> reportComment, Report report, Comment comment, Post post, Member member) {
        reportComment.add(
                ReportCommentDto.builder()
                        .reportId(report.getId())
                        .postId(report.getReportPostId())
                        .commentId(report.getCommentId())
                        .content(report.getContent())
                        .memberUrl(member.getImgUrl())
                        .nickname(member.getNickname())
                        .postUrl(post.getImgUrl())
                        .reportCommentContent(comment.getContent())
                        .build());
    }

    private static ReportListDto makeReportList(List<ReportMemberDto> reportMember, List<ReportPostDto> reportPost, List<ReportCommentDto> reportComment) {
        ReportListDto reportList = ReportListDto.builder()
                .memberList(reportMember)
                .postList(reportPost)
                .commentList(reportComment)
                .build();
        return reportList;
    }
}
