package Backend.FinalProject.service;

import Backend.FinalProject.domain.Comment;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.CommentRequestDto;
import Backend.FinalProject.repository.CommentRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;



    // 댓글 작성
    public ResponseDto<?> writeComment(Long postId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            return ResponseDto.fail("INVALID POST_ID", "잘못된 모임 아이디 입니다.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null) {
            return ResponseDto.fail("EMPTY COMMENT", "내용을 기입해주세요");
        }

        Comment comment = Comment.builder()
                .content(commentDto)
                .member(member)
                .post(post)
                .build();

        commentRepository.save(comment);
        return ResponseDto.success("댓글 작성이 완료되었습니다.");

    }

    // 댓글 수정
    public ResponseDto<?> editComment(Long commentId, CommentRequestDto commentRequestDto, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        // 작성자 검증
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (comment == null) {
            return ResponseDto.fail("NOT FOUND", "해당 댓글을 찾을 수 없습니다.");
        }
        if (comment.getMember().getId() != member.getId()) {
            return ResponseDto.fail("NO AUTHORITY", "작성자만 수정이 가능합니다.");
        }

        String commentDto = commentRequestDto.getComment();
        if (commentDto == null || commentDto.isEmpty()) {
            return ResponseDto.fail("EMPTY COMMENT", "내용을 기입해주세요");
        }

        comment.update(commentDto);

        return ResponseDto.success("댓글 수정이 완료되었습니다.");

    }


    // RefreshToken 유효성 검사
    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    // T
    private ResponseDto<?> validateCheck(HttpServletRequest request) {

        // RefreshToken 및 Authorization 유효성 검사
        if (request.getHeader("Authorization") == null || request.getHeader("RefreshToken") == null) {
            return ResponseDto.fail("NEED_LOGIN", "로그인이 필요합니다.");
        }
        Member member = validateMember(request);

        // 토큰 유효성 검사
        if (member == null) {
            return ResponseDto.fail("INVALID TOKEN", "Token이 유효하지 않습니다.");
        }
        return ResponseDto.success(member);
    }


}
