package Backend.FinalProject.api.service.application;

import Backend.FinalProject.WebSocket.domain.ChatMember;
import Backend.FinalProject.WebSocket.domain.ChatMessage;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.WebSocket.repository.ChatRoomRepository;
import Backend.FinalProject.api.controller.application.request.ApplicationRequestDto;
import Backend.FinalProject.api.service.application.response.ApplicationListResponseDto;
import Backend.FinalProject.api.service.application.response.ApplicationResponseDto;
import Backend.FinalProject.common.AutomatedChatService;
import Backend.FinalProject.common.Tool.Validation;
import Backend.FinalProject.common.dto.ResponseDto;
import Backend.FinalProject.domain.application.Application;
import Backend.FinalProject.domain.application.ApplicationRepository;
import Backend.FinalProject.domain.enums.ApplicationState;
import Backend.FinalProject.domain.member.Member;
import Backend.FinalProject.domain.post.Post;
import Backend.FinalProject.domain.post.PostRepository;
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

import static Backend.FinalProject.common.Tool.Validation.*;
import static Backend.FinalProject.domain.enums.ErrorCode.*;
import static Backend.FinalProject.domain.enums.PostState.CLOSURE;
import static Backend.FinalProject.domain.enums.PostState.DONE;
import static Backend.FinalProject.domain.enums.Regulation.REGULATED;
import static Backend.FinalProject.sse.domain.NotificationType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    //== Dependency Injection ==//
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationRepository applicationRepository;
    private final Validation validation;
    private final NotificationService notificationService;
    private final AutomatedChatService automatedChatService;

    /**
     * 게시글 참여 요청
     * @param postId : 게시글 아이디
     * @param applicationContent : 참여 신청 메세지
     * @param httpServletRequest : HttpServlet Request
     */
    @Transactional
    public ResponseDto<?> submitApplication(Long postId, ApplicationRequestDto applicationContent, HttpServletRequest httpServletRequest) throws Exception {

        // 토큰 유효성 검사
        ResponseDto<?> responseDto = validation.checkAccessToken(httpServletRequest);

        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();

        Optional<Post> optionalPost = postRepository.findById(postId);

        Post post = optionalPost.orElse(null);

        ResponseDto<Object> checkSubmitApplication = handleNull(post, APPLICATION_NOTFOUND);
        if (checkSubmitApplication != null) return checkSubmitApplication;

        ResponseDto<Object> checkMaxNum = handleBoolean(post.getMaxNum() == post.getCurrentNum(), APPLICATION_MAX_NUM);
        if (checkMaxNum !=null) return checkMaxNum;

        ResponseDto<Object> checkContent = handleNull(applicationContent.getContent(), APPLICATION_EMPTY_CONTENT);
        if (checkContent != null) return checkContent;

        // 기존에 참여 신청한 회원의 경우 신청 거절
        Optional<Application> optionalForm = applicationRepository.findByPostIdAndMemberId(postId, member.getId());

        ResponseDto<Object> checkSubmit = handleNotNull(optionalForm.orElse(null), APPLICATION_ALREADY_SUBMIT);
        if (checkSubmit != null) return checkSubmit;

        // 게시글 작성자가 신청을 할 경우 거절
        ResponseDto<Object> checkAccess = handleBoolean(post.getMember().getId().equals(member.getId()), APPLICATION_INVALID_ACCESS);
        if (checkAccess !=null) return checkAccess;

        // 제재먹은 게시글의 경우 신청 불가
        ResponseDto<Object> checkRegulated = handleBoolean(post.getRegulation().equals(REGULATED), APPLICATION_REGULATED_POST);
        if (checkRegulated != null) return checkRegulated;
        // 실시간 알림
        notificationService.send(post.getMember(),APPLY, "새로운 지원 신청이 있습니다.","https://3355.world/detail/" + post.getId()+"/check");

        applicationRepository.save(buildApplication(applicationContent, member, post));
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
        ChatMember chatMember = automatedChatService.createChatMember(application.getMember(), chatRoom);
        // 채팅 메세지
        ChatMessage chatMessage = automatedChatService.createChatMessage(application.getMember(), chatRoom);
        automatedChatService.createReadCheck(chatMember, chatMessage);

        // TODO
        // 수락이 될 경우 참여한 모임 조회 url 로 이동 시키기
        String url = "https://3355.world/mypage";
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
        String url = "https://3355.world/mypage/activity";
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
                            .applicationMemberId(application.getMember().getId())
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
    @Transactional
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
    private static Application buildApplication(ApplicationRequestDto applicationContent, Member member, Post post) {
        Application application = Application.builder()
                .status(ApplicationState.WAIT)
                .content(applicationContent.getContent())
                .member(member)
                .post(post)
                .build();
        return application;
    }
}
