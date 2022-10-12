package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.CommentRequestDto;
import Backend.FinalProject.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 조회
     * @param postId : 게시글 아이디
     */
    @GetMapping("/comment/{postId}")
    public ResponseDto<?> getComments(
            @PathVariable Long postId
    ) {
        return commentService.getComments(postId);
    }


    /**
     * 댓글 작성
     * @param postId : 게시글 아이디
     * @param commentRequestDto : 작성 내용
     * @param request : Token 이 담긴 데이터
     */
    @PostMapping("/comment/{postId}")
    public ResponseDto<?> writeComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto commentRequestDto,
            HttpServletRequest request) {
        return commentService.writeComment(postId, commentRequestDto, request);
    }

    /**
     * 댓글 수정
     * @param commentId : 댓글 아이디
     * @param commentRequestDto : 수정 내용
     * @param request : Token 이 담긴 데이터
     */
    @PutMapping("/comment/{commentId}")
    public ResponseDto<?> editComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDto,
            HttpServletRequest request) {
        return commentService.editComment(commentId, commentRequestDto, request);
    }

    /**
     * 댓글 삭제
     * @param commentId : 댓글 아이디
     * @param request : Token 이 담긴 데이터
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseDto<?> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {
        return commentService.deleteComment(commentId, request);
    }
}
