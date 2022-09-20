package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.CommentRequestDto;
import Backend.FinalProject.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
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
     */
    @PutMapping("/comment/{commentId}")
    public ResponseDto<?> editComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDto,
            HttpServletRequest request) {
        return commentService.editComment(commentId, commentRequestDto, request);
    }

}
