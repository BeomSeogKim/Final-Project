package Backend.FinalProject.service;


import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.response.application.ApplicantResponseDto;
import Backend.FinalProject.dto.response.mypage.MyActPostResponseDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class MyActService {

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final Validation validation;

    @Transactional(readOnly = true)
    public ResponseDto<?> getApplicantList(HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<ApplicantResponseDto> applicants = new ArrayList<>();

        List<Post> postList = postRepository.findAllByMemberId(member.getId());
        if (postList == null) {
            log.info("MyActService applicantList NO POSTS");
            return ResponseDto.fail("NO POSTS", "아직 주최한 모임이 없습니다.");
        }
        for (Post post : postList) {
            // todo
            List<Application> allApplicants = applicationRepository.findAllByPostIdMyAct(post.getId());
            for (Application applicant : allApplicants) {
                applicants.add(
                        ApplicantResponseDto.builder()
                                .postId(post.getId())
                                .nickname(applicant.getMember().getNickname())
                                .imgUrl(applicant.getMember().getImgUrl())
                                .title(post.getTitle())
                                .state(applicant.getStatus())
                                .build()
                );
            }
        }

        return ResponseDto.success(applicants);
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getApplicationList(HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        List<MyActPostResponseDto> list = new ArrayList<>();
        //todo
        List<Application> postList = applicationRepository.findAllByMemberId(member.getId());
        if (postList.isEmpty()) {
            log.info("MyActService postList NO APPLICATION");
            return ResponseDto.fail("NO APPLICATION", "신청 내역이 존재하지 않습니다.");
        }
        for (Application application : postList) {
            list.add(
                    MyActPostResponseDto.builder()
                            .postId(application.getPost().getId())
                            .title(application.getPost().getTitle())
                            .imgUrl(application.getPost().getImgUrl())
                            .state(application.getStatus())
                            .build()
            );
        }
        return ResponseDto.success(list);
    }
}
