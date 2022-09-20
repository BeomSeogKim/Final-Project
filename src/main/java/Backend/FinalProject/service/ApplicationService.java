package Backend.FinalProject.service;

import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.ApplicationRequestDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sercurity.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final TokenProvider tokenProvider;

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;




    // 게시글 참여 요청
    public ResponseDto<?> submitApplication(Long postId, ApplicationRequestDto applicationRequestDto, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            return ResponseDto.fail("NOT FOUND", "해당 게시글을 찾을 수 없습니다.");
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

    // 게시글 참여 수락
    public ResponseDto<?> approveApplication(Long applicationId, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

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
        application.approve();
        return ResponseDto.success("성공적으로 승인이 되었습니다.");
    }

    public ResponseDto<?> disapproveApplication(Long applicationId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validateCheck(request);

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






    // RefreshToken 유효성 검사
    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    // T
    private ResponseDto<?> validateCheck(HttpServletRequest request) {

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
