package Backend.FinalProject.service;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.domain.Application;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.domain.Post;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.dto.response.application.ApplicationListResponseDto;
import Backend.FinalProject.dto.response.application.ApplicationResponseDto;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.dto.request.application.ApplicationRequestDto;
import Backend.FinalProject.repository.ApplicationRepository;
import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Backend.FinalProject.domain.enums.PostState.CLOSURE;
import static Backend.FinalProject.domain.enums.PostState.DONE;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
import static Backend.FinalProject.sse.domain.NotificationType.ACCEPT;
import static Backend.FinalProject.sse.domain.NotificationType.REJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    // Dependency Injection
    private final Validation validation;
    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final AutomatedChatService automatedChatService;
    private final NotificationService notificationService;

    // 게시글 참여 요청
    public ResponseDto<?> submitApplication(Long postId, ApplicationRequestDto applicationRequestDto, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);

        Post post = optionalPost.orElse(null);

        if (post == null) {
            log.info("ApplicationService submitApplication NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 게시글을 찾을 수 없습니다.");
        }
        if (post.getMaxNum() == post.getCurrentNum()) {
            log.info("ApplicationService submitApplication MAX NUM");
            return ResponseDto.fail("MAX NUM", "이미 정원이 다 찼습니다");
        }

        String content = applicationRequestDto.getContent();
        if (content == null) {
            log.info("ApplicationService submitApplication EMPTY");
            return ResponseDto.fail("EMPTY CONTENT", "내용을 적어주세요");
        }

        // 기존에 참여 신청한 회원의 경우 신청 거절
        Optional<Application> optionalForm = applicationRepository.findByPostIdAndMemberId(postId, member.getId());
        Application form = optionalForm.orElse(null);
        if (form != null) {
            log.info("ApplicationService submitApplication ALREADY SUBMIT");
            return ResponseDto.fail("ALREADY SUBMIT", "이미 신청을 하셨습니다.");
        }

        // 게시글 작성자가 신청을 할 경우 거절
        if (post.getMember().getId().equals(member.getId())) {
            log.info("ApplicationService submitApplication INVALID ACCESS");
            return ResponseDto.fail("INVALID ACCESS", "모임 주최자는 신청할 수 없습니다.");
        }

        // 제재먹은 게시글의 경우 신청 불가
        if (post.getRegulation().equals(REGULATED)) {
            log.info("ApplicationService submitApplication REGULATED POST");
            return ResponseDto.fail("REGULATED POST", "관리자에 의해 제재당한 게시글입니다.");
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

        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            log.info("ApplicationService cancelApplication EMPTY");
            return ResponseDto.fail("EMPTY", "해당 게시글이 존재하지 않습니다.");
        }

        if (applicationRepository.findByPostIdAndMemberId(postId, member.getId()).isEmpty()) {
            log.info("ApplicationService cancelApplication NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 게시글에 참여신청한 이력이 없습니다.");
        }

        applicationRepository.deleteByPostIdAndMemberId(postId, member.getId());

        return ResponseDto.success("참여 신청이 취소 되었습니다.");
    }

    // 게시글 참여 수락
    @Transactional
    public ResponseDto<?> approveApplication(Long applicationId, HttpServletRequest request) throws Exception {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        Application application = optionalApplication.orElse(null);
        if (application == null) {
            log.info("ApplicationService approveApplication NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 신청 내역이 없습니다");
        }

        // 모임 주최자만 권한 부여
        if (application.getPost().getMember().getId() != member.getId()) {
            log.info("ApplicationService approveApplication NO AUTHORIZATION");
            return ResponseDto.fail("NO AUTHORIZATION", "권한이 없습니다.");
        }
        if (LocalDate.now().isAfter(application.getPost().getEndDate()) || LocalDate.now().isAfter(application.getPost().getDDay())) {
            log.info("ApplicationService approveApplication PAST DUE");
            return ResponseDto.fail("PAST DUE", "이미 마감된 모임입니다.");
        }

        if (application.getStatus() == ApplicationState.APPROVED) {
            log.info("ApplicationService approveApplication ALREADY APPROVED");
            return ResponseDto.fail("ALREADY APPROVED", "이미 수락을 하셨습니다.");
        }
        if (application.getPost().getCurrentNum() >= application.getPost().getMaxNum()) {
            log.info("ApplicationService approveApplication OVER MAX_NUM");
            return ResponseDto.fail("OVER MAX_NUM", "정원을 초과하였습니다.");
        }
        if (application.getPost().getCurrentNum() < application.getPost().getMaxNum()) {
            application.approve();
        }

        ChatRoom chatRoom = chatRoomRepository.findByPostId(application.getPost().getId()).orElse(null);
        if (chatRoom == null) {
            log.info("ApplicationService approveApplication NO CHAT ROOM");
            return ResponseDto.fail("NO CHAT ROOM", "입장 가능한 채팅방이 존재하지 않습니다.");
        }
        // 채팅방 참여
        automatedChatService.createChatMember(application.getMember(), chatRoom);
        // 채팅 메세지
        automatedChatService.createChatMessage(application.getMember(), chatRoom);

        // TODO
        // 수락이 될 경우 참여한 모임 조회 url 로 이동 시키기
        String url = "http://localhost:3000/mypage";
        notificationService.send(application.getMember(), ACCEPT, application.getPost().getTitle() + " 모임 신청이 수락되었습니다.", url);

        return ResponseDto.success("성공적으로 승인이 되었습니다.");
    }

    @Transactional
    public ResponseDto<?> disapproveApplication(Long applicationId, HttpServletRequest request) throws Exception {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        Application application = optionalApplication.orElse(null);
        if (application == null) {
            log.info("ApplicationService disapproveApplication NOT FOUND");
            return ResponseDto.fail("NOT FOUND", "해당 신청 내역이 없습니다");
        }
        // 모임 주최자만 권한 부여
        if (application.getPost().getMember().getId() != member.getId()) {
            log.info("ApplicationService disapproveApplication NO AUTHORIZATION");
            return ResponseDto.fail("NO AUTHORIZATION", "권한이 없습니다.");
        }
        application.disapprove();
        // TODO
        String url = "http://localhost:3000/mypage/activity";
        notificationService.send(application.getMember(), REJECT, application.getPost().getTitle() + " 모임 신청이 거절되었습니다.", url);
        return ResponseDto.success("성공적으로 거절 되었습니다.");
    }

    // 지원자 보기
    @Transactional(readOnly = true)
    public ResponseDto<?> getApplicationList(Long postId, HttpServletRequest request) {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);
        Post post = optionalPost.orElse(null);
        if (post == null) {
            log.info("ApplicationService getApplicationList EMPTY");
            return ResponseDto.fail("EMPTY", "해당 게시글이 존재하지 않습니다.");
        }

        if (post.getMember().getId() != member.getId()) {
            log.info("ApplicationService getApplicationList NO AUTHORIZATION");
            return ResponseDto.fail("NO AUTHORIZATION", "접근 권한이 없습니다");
        }

        //todo
        List<Application> applicationList = applicationRepository.findAllByPostIdApplication(postId);
        if (applicationList.isEmpty()) {
            log.info("ApplicationService getApplicationList NO ATTENDEE");
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

    // 모집 관련 로직
    public ResponseDto<?> changePostStatus(Long postId, HttpServletRequest request) {
        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(request);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseDto.fail("NO POST", "해당 게시글이 존재하지 않습니다.");
        }
        if (!member.getId().equals(post.getMember().getId())) {
            return ResponseDto.fail("NO AUTHORITY", "게시글 작성자만 모집 마감을 진행 할 수 있습니다.");
        }
        if (post.getCurrentNum() == 0 || post.getStatus() == DONE || post.getStatus() == CLOSURE ||
                post.getRegulation() == REGULATED) {
            return ResponseDto.fail("INVALID REQUIREMENT", "해당 게시글은 모집 마감을 할 수 없습니다.");
        }

        post.updateStatus();
        postRepository.flush();
        return ResponseDto.success("모집이 마감 되었습니다.");

    }
}
