package Backend.FinalProject.controller;

import Backend.FinalProject.request.PostRequestDto;
import Backend.FinalProject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/post")  // 게시글 작성
    public ResponseDto<?> creatpost(@RequestBody PostRequestDto requestDto) {
        return postService.createpost(requestDto);
    }

    @GetMapping(value = "/post")  // 전체게시글 조회
    public ResponseDto<?> getpost(@RequestBody PostRequestDto requestDto) {
        return postService.getpost(requestDto);
    }

    @GetMapping(value = "/post/{id}")   // 상세게시글 조회
    public ResponseDto<?> getpostid(@RequestBody PostRequestDto requestDto) {
        return postService.getpostid(requestDto);
    }

    @PutMapping(value = "/post/{id}")    //게시글 업데이트
    public ResponseDto<?> updatepost(@RequestBody PostRequestDto requestDto) {
        return postService.updatepost(requestDto);
    }

    @DeleteMapping(value = "/post/{id}")   // 게시글 삭제
    public ResponseDto<?> deletepost(@RequestBody PostRequestDto requestDto) {
        return postService.deletepost(requestDto);
    }


    @PostMapping(value = "/post/wish/{id}")    // 게시글 찜
    public ResponseDto<?> wishpost(@RequestBody PostRequestDto requestDto) {
        return postService.wishpost(requestDto);
    }

    @PostMapping(value = "/post/application/{id}")    //게시글 참여 신청
    public ResponseDto<?> application(@RequestBody PostRequestDto requestDto) {
        return postService.application(requestDto);
    }

    @GetMapping(value = "/post/application/{id}")     // 지원자 보기
    public ResponseDto<?> getapplication(@RequestBody PostRequestDto requestDto) {
        return postService.getapplication(requestDto);
    }

    @PostMapping(value = "/post/application/{id}")    // 게시글 참여 수락
    public ResponseDto<?> approve(@RequestBody PostRequestDto requestDto) {
        return postService.approve(requestDto);
    }

    @PostMapping(value = "/post/application/{id}")     // 게시글 참여 거절
    public ResponseDto<?> deny(@RequestBody PostRequestDto requestDto) {
        return postService.deny(requestDto);
    }
}
