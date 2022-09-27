package Backend.FinalProject.service;


import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.response.ApplicantResponseDto;
import Backend.FinalProject.dto.response.MyActPostResponseDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyActService {

    private final TokenProvider tokenProvider;
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;


    public ResponseDto<?> applicantList(HttpServletRequest request) {
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<ApplicantResponseDto> applicants = new ArrayList<>();

        List<Post> postList = postRepository.findAllByMemberId(member.getId());
        if (postList == null) {
            return ResponseDto.fail("NO POSTS", "아직 주최한 모임이 없습니다.");
        }
        for (Post post : postList) {
            System.out.println(post.getTitle());
            List<Application> allApplicants = applicationRepository.findAllByPostId(post.getId()).orElse(null);
            if (allApplicants == null) {
                continue;
            }
            for (Application applicant : allApplicants) {
                applicants.add(
                        ApplicantResponseDto.builder()
                                .postId(post.getId())
                                .nickname(applicant.getMember().getNickname())
                                .title(post.getTitle())
                                .state(applicant.getStatus())
                                .build()
                );
            }
        }

        return ResponseDto.success(applicants);
    }

    public ResponseDto<?> postList(HttpServletRequest request) {

        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<MyActPostResponseDto> list = new ArrayList<>();
        List<Application> postList = applicationRepository.findAllByMemberId(member.getId()).orElse(null);
        if (postList == null) {
            return ResponseDto.fail("NO APPLICATION", "신청 내역이 존재하지 않습니다.");
        }
        for (Application application : postList) {
            list.add(
                    MyActPostResponseDto.builder()
                            .postId(application.getPost().getId())
                            .title(application.getPost().getTitle())
                            .state(application.getStatus())
                            .build()
            );
        }
        return ResponseDto.success(list);
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
