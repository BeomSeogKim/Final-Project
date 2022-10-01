package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.dto.ApplicationListResponseDto;
import Backend.FinalProject.dto.ApplicationResponseDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ApplicationRequestDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

//    private final TokenProvider tokenProvider;
    private final Validation validation;
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;




    // 게시글 참여 요청
    public ResponseDto<?> submitApplication(Long postId, ApplicationRequestDto applicationRequestDto, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);

        Post post = optionalPost.orElse(null);

        if (post == null) {
            return ResponseDto.fail("NOT FOUND", "해당 게시글을 찾을 수 없습니다.");
        }
        if (post.getMaxNum() == post.getCurrentNum()) {
            return ResponseDto.fail("MAX NUM", "이미 정원이 다 찼습니다");
        }

        String content = applicationRequestDto.getContent();
        if (content == null) {
            return ResponseDto.fail("EMPTY", "내용을 적어주세요");
        }

        // 기존에 참여 신청한 회원 거절
        Optional<Application> optionalForm = applicationRepository.findByPostIdAndMemberId(postId, member.getId());
        Application form = optionalForm.orElse(null);
        if (form != null) {
            return ResponseDto.fail("ALREADY SUBMIT", "이미 신청을 하셨습니다.");
        }

        // 게시글 작성자가 신청을 할 경우 거절
        if (post.getMember().getId() == member.getId()) {
            System.out.println("post = " + post.getMember().getId());
            System.out.println("member = " + member.getId());
            return ResponseDto.fail("INVALID ACCESS", "모임 주최자는 신청할 수 없습니다.");
        }


        Application application = Application.builder()
                .status(ApplicationState.WAIT)
                .content(content)
                .member(member)
                .post(post)
                .build();

        applicationRepository.save(application);
        return ResponseDto.success("성공적으로 참여신청을 완료했습니다.");
    }

    // 지원 취소
    @Transactional
    public ResponseDto<?> cancelApplication(Long postId, HttpServletRequest request) {

        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            return ResponseDto.fail("EMPTY", "해당 게시글이 존재하지 않습니다.");
        }

        if (applicationRepository.findByPostIdAndMemberId(postId, member.getId()).isEmpty()) {
            return ResponseDto.fail("NOT FOUND", "해당 게시글에 참여신청한 이력이 없습니다.");
        }

        applicationRepository.deleteByPostIdAndMemberId(postId, member.getId());

        return ResponseDto.success("참여 신청이 취소 되었습니다.");
    }

    // 게시글 참여 수락
    public ResponseDto<?> approveApplication(Long applicationId, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        Application application = optionalApplication.orElse(null);
        if (application == null) {
            return ResponseDto.fail("NOT FOUND", "해당 신청 내역이 없습니다");
        }

        // 모임 주최자만 권한 부여
        if (application.getPost().getMember().getId() != member.getId()) {
            return ResponseDto.fail("NO AUTHORIZATION", "권한이 없습니다.");
        }
        if (LocalDate.now().isAfter(application.getPost().getEndDate()) || LocalDate.now().isAfter(application.getPost().getDDay())) {
            return ResponseDto.fail("PAST DUE", "이미 마감된 모임입니다.");
        }

        if (application.getStatus() == ApplicationState.APPROVED) {
            return ResponseDto.fail("ALREADY APPROVED", "이미 수락을 하셨습니다.");
        }
        if (application.getPost().getCurrentNum() >= application.getPost().getMaxNum()) {
            return ResponseDto.fail("OVER MAX_NUM", "정원을 초과하였습니다.");
        }
        if (application.getPost().getCurrentNum() < application.getPost().getMaxNum()) {
            application.approve();
        }

        return ResponseDto.success("성공적으로 승인이 되었습니다.");
    }

    public ResponseDto<?> disapproveApplication(Long applicationId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        Application application = optionalApplication.orElse(null);
        if (application == null) {
            return ResponseDto.fail("NOT FOUND", "해당 신청 내역이 없습니다");
        }
        // 모임 주최자만 권한 부여
        if (application.getPost().getMember().getId() != member.getId()) {
            return ResponseDto.fail("NO AUTHORIZATION", "권한이 없습니다.");
        }


        application.disapprove();
        return ResponseDto.success("성공적으로 거절 되었습니다.");
    }

    // 지원자 보기
    @Transactional(readOnly = true)
    public ResponseDto<?> getApplicationList(Long postId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            return ResponseDto.fail("EMPTY", "해당 게시글이 존재하지 않습니다.");
        }

        if (post.getMember().getId() != member.getId()) {
            return ResponseDto.fail("NO AUTHORIZATION", "접근 권한이 없습니다");
        }

        Optional<List<Application>> optionalApplicationList = applicationRepository.findAllByPostId(postId);
        List<Application> applicationList = optionalApplicationList.orElse(null);
        if (applicationList == null) {
            return ResponseDto.fail("NO ATTENDEE", "지원자가 존재하지 않습니다.");
        }
        List<ApplicationListResponseDto> applicationListResponseDtoList = new ArrayList<>();

        for (Application application : applicationList) {
            applicationListResponseDtoList.add(
                    ApplicationListResponseDto.builder()
                            .applicationId(application.getId())
                            .nickname(application.getMember().getNickname())
                            .imgUrl(application.getMember().getImgUrl())
                            .state(application.getStatus())
                            .postId(application.getPost().getId())
                            .content(application.getContent())
                            .build()
            );

        }
        return ResponseDto.success(
                ApplicationResponseDto.builder()
                        .title(post.getTitle())
                        .currentNum(post.getCurrentNum())
                        .maxNum(post.getMaxNum())
                        .applicants(applicationListResponseDtoList)
                        .build()
        );
    }
}
