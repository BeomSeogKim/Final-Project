package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.Report;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ReportDto;
import Backend.FinalProject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final MemberRepository memberRepository;

    private  final ReportRepository reportRepository;
    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    private final Validation validation;

    @Transactional
    public  ResponseDto<?> reportUser(Long memberId, ReportDto reportDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        // todo
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 신고할 회원 찾기
        Member reportMember = memberRepository.findById(memberId).orElse(null);
        if (reportRepository.findByMemberId(memberId)!= null) {
            log.info("ReportService reportPost NOT_FOUND");
            return ResponseDto.fail("ALREADY EXIST", "이미 신고하신 회원 입니다.");
        }
        if (reportMember == null) {
            log.info("ReportService reportUser NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 회원을 찾을 수 없습니다.");
        }
        if (reportDto.getContent() == null || reportDto.getContent().isEmpty()) {
            log.info("ReportService reportUser NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "신고내용을 입력해주세요.");
        }

        Report report = Report.builder()
                .content(reportDto.getContent())
                .memberId(memberId)
                .build();

        reportRepository.save(report);

        return ResponseDto.success("신고 접수가 완료되었습니다.");
    }

    @Transactional
    public  ResponseDto<?> reportPost(Long postId, ReportDto reportDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        // todo
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 신고할 게시글 찾기
        Post reportPost = postRepository.findById(postId).orElse(null);
        if (reportRepository.findByPostId(postId) != null) {
            log.info("ReportService reportPost NOT_FOUND");
            return ResponseDto.fail("ALREADY EXIST", "이미 신고하신 게시글 입니다.");
        }
        if (reportPost == null) {
            log.info("ReportService reportPost NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 게시글을 찾을 수 없습니다.");
        }
        if (reportDto.getContent() == null || reportDto.getContent().isEmpty()) {
            log.info("ReportService reportPost NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "신고내용을 입력해주세요.");
        }



        Report report = Report.builder()
                .content(reportDto.getContent())
                .postId(postId)
                .build();


        reportRepository.save(report);

        return ResponseDto.success("신고 접수가 완료되었습니다.");
    }

    @Transactional
    public  ResponseDto<?> reportComment(Long commentId, ReportDto reportDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        // todo
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 신고할 회원 찾기
        Comment reportComment = commentRepository.findById(commentId).orElse(null);
        if (reportRepository.findByCommentId(commentId)!=null) {
            log.info("ReportService reportPost NOT_FOUND");
            return ResponseDto.fail("ALREADY EXIST", "이미 신고하신 댓글 입니다.");
        }
        if (reportComment == null) {
            log.info("ReportService reportComment NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 댓글을 찾을 수 없습니다.");
        }
        if (reportDto.getContent() == null || reportDto.getContent().isEmpty()) {
            log.info("ReportService reportComment NOT_FOUND");
            return ResponseDto.fail("NOT FOUND", "신고내용을 입력해주세요.");
        }

        Report report = Report.builder()
                .content(reportDto.getContent())
                .commentId(commentId)
                .build();

        reportRepository.save(report);

        return ResponseDto.success("신고 접수가 완료되었습니다.");
    }
}
