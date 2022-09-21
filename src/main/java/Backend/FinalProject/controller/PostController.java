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
     *
     * @param request            게시글에 필요한 항목들
     * @param imgFile            이미지 사진
     * @param httpServletRequest Member 검증에 필요한 헤더
     */
    @PostMapping("/post")
    public ResponseDto<?> createPost(
            @RequestPart PostRequestDto request,
            @RequestPart(required = false) MultipartFile imgFile,
            HttpServletRequest httpServletRequest
    ) {
        return postService.createPost(request, imgFile, httpServletRequest);
    }

    @GetMapping("/post")
    public ResponseDto<?> getAllPost() {
        return postService.getAllPost();
    }

    @GetMapping("/post/{id}")
    public ResponseDto<?> getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @PutMapping("/post/{id}")
    public ResponseDto<?> updatePost(@PathVariable Long id, @RequestBody PostUpdateRequestDto requestDto, HttpServletRequest httpServletRequest) {
        return postService.updatePost(id, requestDto, httpServletRequest);
    }

    @DeleteMapping("/post/{id}")
    public ResponseDto<?> deletePost(@PathVariable Long id, HttpServletRequest httpServletRequest){
        return postService.deletePost(id, httpServletRequest);
    }
}
