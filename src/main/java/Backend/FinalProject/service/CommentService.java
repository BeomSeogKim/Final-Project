package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.CommentRequestDto;
import Backend.FinalProject.dto.response.AllCommentResponseDto;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final Validation validation;


    public ResponseDto<?> getComments(Long postId) {
        List<Comment> commentList = commentRepository.findAllByPostId(postId);
//        List<Comment> commentList = commentRepository.findAllByPostIdTest(UNREGULATED, postId);
        if (commentList.isEmpty() || commentList == null) {
            log.info("CommentService getComments NO CONTENT");
            return ResponseDto.fail("NO CONTENT", "댓글이 존재하지 않습니다.");
        }
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
                                .build()
                );
            }
        }

        return ResponseDto.success(commentResponseDto);
    }


    // 댓글 작성
    public ResponseDto<?> writeComment(Long postId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

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
        return ResponseDto.success("댓글 작성이 완료되었습니다.");

    }

    // 댓글 수정
    public ResponseDto<?> editComment(Long commentId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 작성자 검증
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            log.info("CommentService eidtComment NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 댓글을 찾을 수 없습니다.");
        }
        if (comment.getMember().getId() != member.getId()) {
            log.info("CommentService eidtComment NO AUTHORITY");
            return ResponseDto.fail("NO AUTHORITY", "작성자만 수정이 가능합니다.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null || commentDto.isEmpty()) {
            log.info("CommentService eidtComment EMPTY COMMENT");
            return ResponseDto.fail("EMPTY COMMENT", "내용을 기입해주세요");
        }

        comment.update(commentDto);

        return ResponseDto.success("댓글 수정이 완료되었습니다.");

    }

    // 댓글 삭제
    public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

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
