package Backend.FinalProject.api.controller.myPage;

import Backend.FinalProject.api.service.myPage.MyPageService;
import Backend.FinalProject.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    //== Dependency Injection ==//
    private final MyPageService myPageService;

    /**
     * 참여중인 모임 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/participation")
    public ResponseDto<?> getParticipationList(HttpServletRequest httpServletRequest) {
        return myPageService.getParticipationList(httpServletRequest);
    }

    /**
     * 찜한 모임 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/wish")
    public ResponseDto<?> getWishList(HttpServletRequest httpServletRequest) {
        return myPageService.getWishList(httpServletRequest);
    }

    /**
     * 내가 주최한 모임 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/leader")
    public ResponseDto<?> getPostLedByMe(HttpServletRequest httpServletRequest) {
        return myPageService.getPostLedByMe(httpServletRequest);
    }

    /**
     * 내 정보 조회
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/info")
    public ResponseDto<?> getMyInfo(HttpServletRequest httpServletRequest) {
        return myPageService.getMyInfo(httpServletRequest);
    }

    /**
     * 회원 정보 조회
     * @param memberId : 회원 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/{memberId}")
    public ResponseDto<?> getMemberInfo(@PathVariable Long memberId,
                                        HttpServletRequest httpServletRequest) {
        return myPageService.getMemberInfo(memberId,httpServletRequest);
    }

    /**
     * 해당 회원이 작성한 게시글 조회
     * @param memberId : 회원 아이디
     * @param httpServletRequest : HttpServlet Request
     */
    @GetMapping("/mypage/leader/{memberId}")
    public ResponseDto<?> getMemberPostList(@PathVariable Long memberId,
                                            HttpServletRequest httpServletRequest) {
        return myPageService.getMemberPostList(memberId,httpServletRequest);
    }
}

