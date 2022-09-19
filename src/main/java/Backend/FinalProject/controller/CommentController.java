package Backend.FinalProject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping(value = "/comment/{postid}")   // 댓글 작성
    public ResponseDto<?> creatcommnet(@RequestBody CommentRequestDto requestDto) {
        return commentService.creatcommnet(requestDto);
    }

    @PutMapping(value = "/comment/{commentid}")    // 댓글 수정
    public ResponseDto<?> updatecommnet(@RequestBody CommentRequestDto requestDto) {
        return commentService.updatecommnet(requestDto);
    }

    @DeleteMapping(value = "/comment/{commentid}")    // 댓글 삭제
    public ResponseDto<?> deletecommnet(@RequestBody CommentRequestDto requestDto) {
        return commentService.deletecommnet(requestDto);
    }
}
