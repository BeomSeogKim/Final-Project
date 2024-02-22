package Backend.FinalProject.api.controller.post;

import Backend.FinalProject.api.controller.post.request.PostRequestDto;
import Backend.FinalProject.api.controller.post.request.PostUpdateRequestDto;
import Backend.FinalProject.api.controller.post.request.SearchDto;
import Backend.FinalProject.api.service.post.PostService;
import Backend.FinalProject.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class PostController {

    //== Dependency Injection ==//
    private final PostService postService;

    /**
     * 게시글 작성
     * @param postRequestDto : 작성에 필요한 데이터
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/post")
    public ResponseDto<?> writePost(
            @ModelAttribute PostRequestDto postRequestDto,
            HttpServletRequest httpServletRequest) {
        return postService.writePost(postRequestDto, httpServletRequest);
    }

    /**
     * 전체 게시글 조회
     * @param pageNum : 페이지 장 수
     */
    @GetMapping("/post/all")
    public ResponseDto<?> getPostList(@RequestParam("page") Integer pageNum) {
        return postService.getPostList(pageNum);
    }

    /**
     * 전체 게시글 조회(D-day순으로 정렬)
     * @param pageNum : 페이지 장 수
     */
    @GetMapping("/post/all/day")
    public ResponseDto<?> getPostDdayList(@RequestParam("page") Integer pageNum) {
        return postService.getPostDdayList(pageNum);
    }

    /**
     * 상세 게시글 조회
     * @param postId : 게시글 아이디
     */
    @GetMapping("/post/detail/{postId}")
    public ResponseDto<?> getDetailPost(@PathVariable Long postId) {
        return postService.getDetailPost(postId);
    }

    /**
     * 게시글 업데이트
     * @param postId : 게시글 아이디
     * @param requestDto : 업데이트에 필요한 데이터
     * @param httpServletRequest : HttpServlet Request
     */
    @PutMapping("/post/{postId}")
    public ResponseDto<?> updatePost(@PathVariable Long postId,
                                     @ModelAttribute PostUpdateRequestDto requestDto,
                                     HttpServletRequest httpServletRequest) {
        return postService.updatePost(postId, requestDto, httpServletRequest);
    }

    /**
     * 게시글 삭제
     * @param postId : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @DeleteMapping("/post/{postId}")
    public ResponseDto<?> deletePost(@PathVariable Long postId,
                                     HttpServletRequest httpServletRequest){
        return postService.deletePost(postId, httpServletRequest);
    }

    /**
     * 게시글 검색
     * @param searchDto : keyword 및 category 검색
     * @param pageNum : 페이지네이션을 위한 page 숫자.
     */
    @PostMapping("/post/search")
    public ResponseDto<?> findPost(@RequestBody SearchDto searchDto,
                                   @RequestParam("page") Integer pageNum) {
        return postService.findPost(searchDto, pageNum);
    }

    /**
     * 게시글 찜
     * @param postId : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/post/add/wish/{postId}")
    public ResponseDto<?> addWishList(@PathVariable Long postId,
                                      HttpServletRequest httpServletRequest) {
        return postService.addWishList(postId, httpServletRequest);
    }

    /**
     * 게시글 찜 삭제
     * @param postId : 게시글 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @PostMapping("/post/remove/wish/{postId}")
    public ResponseDto<?> removeWishList(@PathVariable Long postId,
                                  HttpServletRequest httpServletRequest){
        return postService.removeWishList(postId, httpServletRequest);
    }
}
