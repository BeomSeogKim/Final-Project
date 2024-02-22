package Backend.FinalProject.api.service.comment;

import Backend.FinalProject.api.controller.comment.request.CommentRequestDto;
import Backend.FinalProject.api.service.comment.response.AllCommentResponseDto;
import Backend.FinalProject.common.Tool.Validation;
import Backend.FinalProject.common.dto.ResponseDto;
import Backend.FinalProject.domain.comment.Comment;
import Backend.FinalProject.domain.comment.CommentRepository;
import Backend.FinalProject.domain.member.Member;
import Backend.FinalProject.domain.post.Post;
import Backend.FinalProject.domain.post.PostRepository;
import Backend.FinalProject.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static Backend.FinalProject.domain.enums.Regulation.UNREGULATED;
import static Backend.FinalProject.sse.domain.NotificationType.REPLY;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final Validation validation;
    private final NotificationService notificationService;


    public ResponseDto<?> getCommentList(Long postId) {
        List<Comment> commentList = commentRepository.findAllByPostId(postId);
        List<AllCommentResponseDto> commentResponseDto = new ArrayList<>();
        for (Comment comment : commentList) {
            if (comment.getRegulation().equals(UNREGULATED)) {
                commentResponseDto.add(
                        AllCommentResponseDto.builder()
                                .commentId(comment.getId())
                                .memberImage(comment.getMember().getImgUrl())
                                .memberNickname(comment.getMember().getNickname())
                                .content(comment.getContent())
                                .memberId(comment.getMember().getUserId())
                                .commentMemberId(comment.getMember().getId())
                                .build()
                );
            }
        }

        return ResponseDto.success(commentResponseDto);
    }


    // 댓글 작성
    public ResponseDto<?> writeComment(Long postId, CommentRequestDto commentRequestDto, HttpServletRequest request) throws Exception {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            log.info("CommentService writeComment INVALID POST_ID");
            return ResponseDto.fail("INVALID POST_ID", "잘못된 모임 아이디 입니다.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null) {
            log.info("CommentService writeComment EMPTY COMMENT");
            return ResponseDto.fail("EMPTY COMMENT", "내용을 기입해주세요");
        }

        Comment comment = Comment.builder()
                .content(commentDto)
                .member(member)
                .post(post)
                .regulation(UNREGULATED)
                .build();

        commentRepository.save(comment);
        //TODO
        String url = "https://3355.world/detail/" + post.getId();
        if (post.getMember() != member) {
            notificationService.send(post.getMember(), REPLY, member.getNickname()+ "님이 " + post.getTitle() + "게시글에 댓글을 달았습니다.", url);
        }
        return ResponseDto.success("댓글 작성이 완료되었습니다.");

    }

    // 댓글 수정
    public ResponseDto<?> editComment(Long commentId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 작성자 검증
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            log.info("CommentService editComment NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 댓글을 찾을 수 없습니다.");
        }
        if (!comment.getMember().getId().equals(member.getId())) {
            log.info("CommentService editComment NO AUTHORITY");
            return ResponseDto.fail("NO AUTHORITY", "작성자만 수정이 가능합니다.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null || commentDto.isEmpty()) {
            log.info("CommentService editComment EMPTY COMMENT");
            return ResponseDto.fail("EMPTY COMMENT", "내용을 기입해주세요");
        }

        comment.editComment(commentDto);

        return ResponseDto.success("댓글 수정이 완료되었습니다.");

    }

    // 댓글 삭제
    public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 작성자 검증
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            log.info("CommentService deleteComment NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 댓글을 찾을 수 없습니다.");
        }
        String role = request.getHeader("role");
        if (!Objects.equals(role, "ROLE_ADMIN")) {
            if (!Objects.equals(comment.getMember().getId(), member.getId())) {
                log.info("CommentService deleteComment NO AUTHORITY");
                return ResponseDto.fail("NO AUTHORITY", "작성자만 수정이 가능합니다.");
            }
        }


        commentRepository.delete(comment);
        return ResponseDto.success("댓글 삭제가 완료되었습니다.");
    }
}
