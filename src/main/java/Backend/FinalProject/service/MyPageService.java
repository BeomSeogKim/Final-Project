package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Time;
import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.WishList;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.response.MyPageDto;
import Backend.FinalProject.dto.response.WishListDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.WishListRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyPageService {

    private final TokenProvider tokenProvider;

    private final ApplicationRepository applicationRepository;

    private final PostRepository postRepository;

    private final WishListRepository wishListRepository;

    Time time = new Time();

    // 신청한 모임 조회
    public ResponseDto<?> application(HttpServletRequest request) {

        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<Application> applicationList = applicationRepository.findAllByMemberId(member.getId()).orElse(null);
        if (applicationList == null) {
            return ResponseDto.fail("NOT FOUND", "신청한 모임이 없습니다.");
        }

        return ResponseDto.success(applicationList);


    }

    // 참여 중인 모임 조회
    public ResponseDto<?> participation(HttpServletRequest request) {

        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        List<Application> applicationList = applicationRepository.findAllByMemberId(member.getId()).orElse(null);
        if (applicationList == null) {
            return ResponseDto.fail("NOT FOUND", "신청한 모임이 없습니다.");
        }

        List<MyPageDto> mypageListDto = new ArrayList<>();

        for (Application application : applicationList) {
            if (application.getStatus() == null) {
                return ResponseDto.fail("NOT FOUND", "참여중인 모임이 없습니다");
            }
            if (application.getStatus().equals(ApplicationState.APPROVED)) {
                mypageListDto.add(
                        MyPageDto.builder()
                                .postId(application.getPost().getId())
                                .title(application.getPost().getTitle())
                                .address(application.getPost().getAddress())
                                .dDay(application.getPost().getDDay())
                                .restDay(time.convertLocalDateToTime(application.getPost().getEndDate()))
                                .imgUrl(application.getPost().getImgUrl())
                                .nickname(application.getMember().getNickname())
                                .build());
            }
        }

        List<Post> postList = postRepository.findAllByMemberId(member.getId());
        for (Post post : postList) {
            mypageListDto.add(
                    MyPageDto.builder()
                            .postId(post.getId())
                            .title(post.getTitle())
                            .address(post.getAddress())
                            .dDay(post.getDDay())
                            .restDay(time.convertLocalDateToTime(post.getEndDate()))
                            .imgUrl(post.getImgUrl())
                            .nickname(post.getMember().getNickname())
                            .build()
            );
        }


        return ResponseDto.success(mypageListDto);
    }



    // 내가 주최한 모임 조회
    public ResponseDto<?> leader(HttpServletRequest request) {

        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<Post> postListById = postRepository.findAllByMemberId(member.getId());
        if (postListById.isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "주최한 모임이 없습니다");
        }


        List<MyPageDto> postList = new ArrayList<>();
        for (Post post : postListById) {
            if (post.getMember().equals(member.getId())) {

            }
            postList.add(
                    MyPageDto.builder()
                            .postId(post.getId())
                            .title(post.getTitle())
                            .address(post.getAddress())
                            .dDay(post.getDDay())
                            .restDay(time.convertLocalDateToTime(post.getEndDate()))
                            .imgUrl(post.getImgUrl())
                            .nickname(post.getMember().getNickname())
                            .build()
            );

        }
        return ResponseDto.success(postList);
    }

    // 찜한 모임 조회
    public ResponseDto<?> addWish(HttpServletRequest request) {
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<WishList> wishLists = wishListRepository.findAllByMemberId(member.getId()).orElse(null);
        List<MyPageDto> wishListDto = new ArrayList<>();

        for (WishList wishList : wishLists) {
            wishListDto.add(
                    MyPageDto.builder()
                            .postId(wishList.getPost().getId())
                            .title(wishList.getPost().getTitle())
                            .address(wishList.getPost().getAddress())
                            .dDay(wishList.getPost().getDDay())
                            .restDay(time.convertLocalDateToTime(wishList.getPost().getEndDate()))
                            .imgUrl(wishList.getPost().getImgUrl())
                            .nickname(wishList.getPost().getMember().getNickname())
                            .build()
            );
        }
        return ResponseDto.success(wishListDto);

    }


        // 토큰
        public Member validateMember (HttpServletRequest request){
            if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
                return null;
            }
            return tokenProvider.getMemberFromAuthentication();
        }
        // 토큰이 있는지 보고 로그인 여부를 판단하는 메소드
        private ResponseDto<?> validateCheck (HttpServletRequest request){

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
