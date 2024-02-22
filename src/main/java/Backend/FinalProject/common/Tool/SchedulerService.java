package Backend.FinalProject.common.Tool;

import Backend.FinalProject.domain.post.Post;
import Backend.FinalProject.domain.post.PostRepository;
import Backend.FinalProject.domain.signoutMember.SignOutMember;
import Backend.FinalProject.domain.signoutMember.SignOutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final PostRepository postRepository;
    private final SignOutRepository signOutRepository;


    /**
     * 매일 오전 1시 기준으로 게시글들 상태 업데이트
     * 모집 마감일이 지났는데 모집 인원이 0이 아닌 게시글의 경우 ==> DONE 상태로 바꿈
     */

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void run() {
        log.info("게시글 상태 업데이트 시작 ");
        LocalDate now = LocalDate.now();

        List<Post> postList = postRepository.findAll();
        for (Post post : postList) {
            // 모집 일자가 지났을 때 모집 인원이 한명이라도 있을 경우 모집 성사로 간주
            if (now.isAfter(post.getEndDate()) && post.getCurrentNum() != 0) {
                post.updateStatus();
            }
            // 모집 일자가 지났을 때 모집 인원이 한명도 없을 경우 모집 성사되지 않음으로 간주.
            if (now.isAfter(post.getEndDate()) && post.getCurrentNum() == 0) {
                post.closeStatus();
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void deleteInformation() {

        log.info("회원 탈퇴 검증 시작");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime criticalLimit = now.minusYears(5);

        List<SignOutMember> signOutMemberList = signOutRepository.findAll();
        for (SignOutMember signOutMember : signOutMemberList) {
            if (criticalLimit.isAfter(signOutMember.getCreatedAt())) {
                signOutRepository.delete(signOutMember);
            }
        }
    }
}
