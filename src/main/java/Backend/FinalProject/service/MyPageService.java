package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Time;
import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.WishList;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.response.MemberInfoDto;
import Backend.FinalProject.dto.response.MyPageDto;
import Backend.FinalProject.dto.response.getMyPageDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.MemberRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.repository.WishListRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
    private final TokenProvider tokenProvider;

    private final ApplicationRepository applicationRepository;

    private final PostRepository postRepository;

    private final WishListRepository wishListRepository;
    private final Validation validation;

    private final MemberRepository memberRepository;

    Time time = new Time();

    // 신청한 모임 조회
    public ResponseDto<?> application(HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        //todo
        List<Application> applicationList = applicationRepository.findAllByMemberId(member.getId());
        if (applicationList.isEmpty()) {
            log.info("MyPageService application NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "신청한 모임이 없습니다.");
        }

        return ResponseDto.success(applicationList);


    }

    // 참여 중인 모임 조회
    public ResponseDto<?> participation(HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }

        Member member = (Member) responseDto.getData();

        //todo
        List<Application> applicationList = applicationRepository.findAllByMemberId(member.getId());
        if (applicationList.isEmpty()) {
            log.info("MyPageService applicationList NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "신청한 모임이 없습니다.");
        }

        List<MyPageDto> mypageListDto = new ArrayList<>();

        for (Application application : applicationList) {
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

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<Post> postListById = postRepository.findAllByMemberId(member.getId());
        if (postListById.isEmpty() || equals(null)) {
            log.info("MyPageService leader NOT FOUND");
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
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<WishList> wishLists = wishListRepository.findAllByMemberId(member.getId()).orElse(null);
        if (wishLists.isEmpty() || equals(null)) {
            log.info("MyPageService addWish NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "찜한 모임이 없습니다");
        }
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

    public ResponseDto<?> getInfo(HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        return ResponseDto.success(MemberInfoDto.builder()
                .nickname(member.getNickname())
                .userId(member.getUserId())
                .imgUrl(member.getImgUrl())
                .root(member.getRoot())
                .build());
    }
    // 회원정보 조회
    public ResponseDto<?> getMemberMypage(Long memberId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.orElse(null);
        if (member == null) {
            log.info("MyPageService getMemberMypage NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 고객은 존재하지 않습니다.");
        }
        List<Application> memberAplicationInfo = applicationRepository.findAllByMemberIdAndStatus(memberId,ApplicationState.APPROVED);
        if (memberAplicationInfo == null) {
            log.info("MyPageService getMemberMypage NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "참가했던 모임이 없습니다.");
        }
        int aplicationCount=0;
        for(int i =0; i<memberAplicationInfo.size();i++){
            aplicationCount++;
        }

        List<Post> PostHostInfo  = postRepository.findAllByMemberId(memberId);
        if (PostHostInfo == null) {
            return ResponseDto.fail("NOT FOUND", "주최했던 모임이 없습니다.");
        }
            int hostCount=0;
            for(int i =0; i<PostHostInfo.size();i++) {
                hostCount++;
            }
        return ResponseDto.success(
                    getMyPageDto.builder()
                            .nickname(member.getNickname())
                            .imgUrl(member.getImgUrl())
                            .root(member.getRoot())
                            .gender(member.getGender())
                            .minAge(member.getMinAge())
                            .aplicationCount(aplicationCount)
                            .leaderCount(hostCount)
                            .numOfRegulation(member.getNumOfRegulation())
                            .build()
            );
        }

        // 해당 회원이 게시한 게시글 조회
    public ResponseDto<?> getMemberPost(Long memberId, HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.orElse(null);
        if (member == null) {
            log.info("MyPageService getMemberPost NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 고객은 존재하지 않습니다.");
        }
        List<Post> postListById = postRepository.findAllByMemberId(member.getId());
        if (postListById.isEmpty() || equals(null)) {
            log.info("MyPageService getMemberPost NOT FOUND");
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
}

