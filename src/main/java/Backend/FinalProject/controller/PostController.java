package Backend.FinalProject.controller;

import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.PostRequestDto;
import Backend.FinalProject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public ResponseDto<?> createPost(
            @RequestPart PostRequestDto requestDto,
            @RequestPart MultipartFile imgFile,
            HttpServletRequest httpServletRequest
    ) {
        return postService.createPost(requestDto, imgFile, httpServletRequest);
    }

    @GetMapping("/post")
    public ResponseDto<?> getAllPost(){
        return postService.getAllPost();
    }

    @GetMapping("/post/{id}")
    public ResponseDto<?> getPost(@PathVariable Long id){
        return postService.getPost(id);
    }

    @PutMapping("/post/{id}")
    public ResponseDto<?> updatePost(@PathVariable Long id, HttpServletRequest httpServletRequest){
        return postService.updatePost(id, httpServletRequest);
    }

    @DeleteMapping("/post/{id}")
    public ResponseDto<?> deletePost(@PathVariable Long id, HttpServletRequest httpServletRequest){
        return postService.deletePost(id, httpServletRequest);
    }
}
