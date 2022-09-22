package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.PostRequestDto;
import Backend.FinalProject.dto.request.PostUpdateRequestDto;
import Backend.FinalProject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    /**
     * 게시글 작성
     * @param postRequestDto 작성에 필요한 데이터
     * @param request Token 이 담긴 데이터
     */
    @PostMapping("/post")
    public ResponseDto<?> createPost(
            @ModelAttribute PostRequestDto postRequestDto,
            HttpServletRequest request) {
        return postService.createPost(postRequestDto, request);
    }

    /**
     * 전체 게시글 조회
     */
    @GetMapping("/post")
    public ResponseDto<?> getAllPost() {
        return postService.getAllPost();
    }

    /**
     * 상세 게시글 조회
     * @param postId : 게시글 아이디
     */
    @GetMapping("/post/{postId}")
    public ResponseDto<?> getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    /**
     * 게시글 업데이트
     * @param postId : 게시글 아이디
     * @param requestDto : 업데이트에 필요한 데이터
     * @param request : Token이 담긴 데이터
     * @return
     */
    @PutMapping("/post/{postId}")
    public ResponseDto<?> updatePost(@PathVariable Long postId,
                                     @RequestBody PostUpdateRequestDto requestDto,
                                     HttpServletRequest request) {
        return postService.updatePost(postId, requestDto, request);
    }

    /**
     * 게시글 삭제
     * @param postId : 게시글 아이디
     * @param request : Token이 담긴 데이터
     * @return
     */
    @DeleteMapping("/post/{postId}")
    public ResponseDto<?> deletePost(@PathVariable Long postId,
                                     HttpServletRequest request){
        return postService.deletePost(postId, request);
    }
}
